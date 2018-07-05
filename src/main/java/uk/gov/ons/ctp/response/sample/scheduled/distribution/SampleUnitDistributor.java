package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.SampleAttributes;

/** Distributes SampleUnits */
@Component
@Slf4j
public class SampleUnitDistributor {
  public static final String SAMPLEUNIT_DISTRIBUTOR_SPAN = "sampleunitDistributor";
  public static final String SAMPLEUNIT_DISTRIBUTOR_LIST_ID = "sampleunit";
  private static final int E = -9999;
  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private SampleAttributesRepository sampleAttributesRepository;

  @Autowired private AppConfig appConfig;

  @Autowired private MapperFacade mapperFacade;

  @Autowired private SampleUnitPublisher sampleUnitPublisher;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
      sampleUnitStateTransitionManager;

  @Autowired private DistributedListManager<Integer> sampleUnitDistributionListManager;

  /** @return SampleUnitDistributionInfo Information for SampelUnit Distribution */
  @Transactional
  public SampleUnitDistributionInfo distribute() {
    log.info("SampleUnitDistributor is in the house");
    SampleUnitDistributionInfo distInfo = new SampleUnitDistributionInfo();

    int successes = 0;
    int failures = 0;
    try {
      List<CollectionExerciseJob> jobs = collectionExerciseJobRepository.findAll();
      for (CollectionExerciseJob job : jobs) {
        List<SampleUnit> sampleUnits;

        List<Integer> excludedCases =
            sampleUnitDistributionListManager.findList(SAMPLEUNIT_DISTRIBUTOR_LIST_ID, false);
        log.debug("retrieve sample units excluding {}", excludedCases);
        if (excludedCases.size() == 0) {
          excludedCases.add(E);
        }

        sampleUnits =
            sampleUnitRepository.getSampleUnits(
                job.getSampleSummaryId(),
                SampleSummaryDTO.SampleState.ACTIVE.toString(),
                appConfig.getSampleUnitDistribution().getRetrievalMax(),
                excludedCases);

        if (sampleUnits.size() > 0) {
          sampleUnitDistributionListManager.saveList(
              SAMPLEUNIT_DISTRIBUTOR_LIST_ID,
              sampleUnits.stream().map(SampleUnit::getSampleUnitPK).collect(Collectors.toList()),
              true);
        }

        for (SampleUnit sampleUnit : sampleUnits) {
          try {
            uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
                mapperFacade.map(
                    sampleUnit, uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);
            uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes =
                sampleAttributesRepository.findOne(sampleUnit.getId());
            if (sampleUnit.getId() != null) {
              mappedSampleUnit.setId(sampleUnit.getId().toString());
            }
            if (sampleAttributes != null) {
              mappedSampleUnit.setSampleAttributes(mapSampleAttributes(sampleAttributes));
            }
            mappedSampleUnit.setCollectionExerciseId(job.getCollectionExerciseId().toString());
            sendSampleUnitToCollectionExerciseQueue(sampleUnit, mappedSampleUnit);

            successes++;
          } catch (CTPException e) {
            // single case/questionnaire db changes rolled back
            log.error(
                "Exception {} thrown processing sampleunit {}. Processing postponed",
                e.getMessage(),
                sampleUnit.getSampleUnitPK());
            log.error("Stack trace: " + e);
            failures++;
          }
        }
        sampleUnitDistributionListManager.deleteList(SAMPLEUNIT_DISTRIBUTOR_LIST_ID, true);
        try {
          sampleUnitDistributionListManager.unlockContainer();
        } catch (LockingException le) {
          // oh well - will time out or we never had the lock
        }
      }
    } catch (Exception e) {
      log.error("Failed to process sample units", e);
    }

    distInfo.setSampleUnitsSucceeded(successes);
    distInfo.setSampleUnitsFailed(failures);

    log.info("SampleUnitsDistributor sleeping");
    return distInfo;
  }

  private SampleAttributes mapSampleAttributes(
      uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes) {
    SampleAttributes mappedSampleAttributes = new SampleAttributes();
    SampleAttributes.Builder<Void> sampleAttributesBuilder =
        mappedSampleAttributes.newCopyBuilder();
    for (Map.Entry<String, String> attribute : sampleAttributes.getAttributes().entrySet()) {
      sampleAttributesBuilder
          .addEntries()
          .withKey(attribute.getKey())
          .withValue(attribute.getValue());
    }
    return sampleAttributesBuilder.build();
  }

  /**
   * Sends SampleUnit to the CollectionExercise queue
   *
   * @param sampleUnit sample unit to be mapped
   * @param mappedSampleUnit mapped sample unit to be sent
   * @throws CTPException if transition issue
   */
  private void sendSampleUnitToCollectionExerciseQueue(
      SampleUnit sampleUnit,
      uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit)
      throws CTPException {
    transitionSampleUnitStateFromDeliveryEvent(sampleUnit);
    sampleUnitPublisher.send(mappedSampleUnit);
  }

  private SampleUnit transitionSampleUnitStateFromDeliveryEvent(SampleUnit sampleUnit)
      throws CTPException {
    SampleUnitDTO.SampleUnitState newState =
        sampleUnitStateTransitionManager.transition(
            sampleUnit.getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
    sampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(sampleUnit);
    return sampleUnit;
  }
}
