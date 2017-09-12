package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Distributes SampleUnits
 */
@Component
@Slf4j
public class SampleUnitDistributor {
  private static final int E = -9999;
  public static final String SAMPLEUNIT_DISTRIBUTOR_SPAN = "sampleunitDistributor";
  public static final String SAMPLEUNIT_DISTRIBUTOR_LIST_ID = "sampleunit";

  @Autowired
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired
  private SampleUnitRepository sampleUnitRepository;

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private MapperFacade mapperFacade;

  @Autowired
  private SampleUnitPublisher sampleUnitPublisher;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
          sampleUnitStateTransitionManager;

  // single TransactionTemplate shared amongst all methods in this instance
  private final TransactionTemplate transactionTemplate;

  @Autowired
  private DistributedListManager<Integer> sampleUnitDistributionListManager;

  /**
   * Constructor into which the Spring PlatformTransactionManager is injected
   *
   * @param transactionManager provided by Spring
   */
  @Autowired
  public SampleUnitDistributor(final PlatformTransactionManager transactionManager) {
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  /**
   * @return SampleUnitDistributionInfo Information for SampelUnit Distribution
   */
  public final SampleUnitDistributionInfo distribute() {
    log.info("SampleUnitDistributor is in the house");
    SampleUnitDistributionInfo distInfo = new SampleUnitDistributionInfo();

    int successes = 0, failures = 0;
    try {
      List<CollectionExerciseJob> jobs = collectionExerciseJobRepository.findAll();
      for (CollectionExerciseJob job : jobs) {
        List<SampleUnit> sampleUnits;

        List<Integer> excludedCases = sampleUnitDistributionListManager.findList(SAMPLEUNIT_DISTRIBUTOR_LIST_ID,
                false);
        log.debug("retrieve sample units excluding {}", excludedCases);
        if (excludedCases.size() == 0) {
          excludedCases.add(E);
        }

        sampleUnits = sampleUnitRepository.getSampleUnitBatch(job.getSurveyRef(),
            job.getExerciseDateTime(), SampleSummaryDTO.SampleState.ACTIVE.toString(),
            appConfig.getSampleUnitDistribution().getRetrievalMax(), excludedCases);

        if (sampleUnits.size() > 0) {
          sampleUnitDistributionListManager.saveList(SAMPLEUNIT_DISTRIBUTOR_LIST_ID, sampleUnits.stream()
              .map(su -> su.getSampleUnitPK())
              .collect(Collectors.toList()), true);
        }

        for (SampleUnit sampleUnit : sampleUnits) {
          try {
            uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit = mapperFacade.map(sampleUnit,
                uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);
            mappedSampleUnit.setCollectionExerciseId(job.getCollectionExerciseId().toString());
            sendSampleUnitToCollectionExerciseQueue(sampleUnit, mappedSampleUnit);
            successes++;
          } catch (CTPException e) {
            // single case/questionnaire db changes rolled back
            log.error("Exception {} thrown processing sampleunit {}. Processing postponed", e.getMessage(),
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
      log.error("Failed to process sample units because {}", e);
    }

    distInfo.setSampleUnitsSucceeded(successes);
    distInfo.setSampleUnitsFailed(failures);

    log.info("SampleUnitsDistributor sleeping");
    return distInfo;
  }

  /**
   * Sends SampleUnit to the CollectionExercise queue
   * @param sampleUnit sample unit to be mapped
   * @param mappedSampleUnit mapped sample unit to be sent
   * @throws CTPException if transition issue
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  private void sendSampleUnitToCollectionExerciseQueue(SampleUnit sampleUnit,
                                                       uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit) throws CTPException {
    transitionSampleUnitStateFromDeliveryEvent(sampleUnit.getSampleUnitPK());
    sampleUnitPublisher.send(mappedSampleUnit);
  }

  /**
   * Transitions SampleUnit State from Delivery Event
   * @param sampleUnitPK sample unit primary key
   * @return SampleUnit the target sampleunit
   * @throws CTPException if transition issue
   */
  public SampleUnit transitionSampleUnitStateFromDeliveryEvent(Integer sampleUnitPK) throws CTPException {
    SampleUnit targetSampleUnit = sampleUnitRepository.findOne(sampleUnitPK);
    SampleUnitDTO.SampleUnitState newState = sampleUnitStateTransitionManager.transition(
            targetSampleUnit.getState(),
        SampleUnitDTO.SampleUnitEvent.DELIVERING);
    targetSampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(targetSampleUnit);
    return targetSampleUnit;
  }

}
