package uk.gov.ons.ctp.response.sample.service;

import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.SampleAttributes;

/** Distributes SampleUnits */
@Component
@Slf4j
public class SampleUnitDistributor {

  private final SampleUnitRepository sampleUnitRepository;

  private final SampleSummaryRepository sampleSummaryRepository;

  private final SampleAttributesRepository sampleAttributesRepository;

  private final MapperFacade mapperFacade;

  private final SampleUnitPublisher sampleUnitPublisher;

  private final StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
      sampleUnitStateTransitionManager;

  public SampleUnitDistributor(
      SampleUnitRepository sampleUnitRepository,
      SampleSummaryRepository sampleSummaryRepository,
      SampleAttributesRepository sampleAttributesRepository,
      MapperFacade mapperFacade,
      SampleUnitPublisher sampleUnitPublisher,
      @Qualifier("sampleUnitTransitionManager")
          StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
              sampleUnitStateTransitionManager) {
    this.sampleUnitRepository = sampleUnitRepository;
    this.sampleSummaryRepository = sampleSummaryRepository;
    this.sampleAttributesRepository = sampleAttributesRepository;
    this.mapperFacade = mapperFacade;
    this.sampleUnitPublisher = sampleUnitPublisher;
    this.sampleUnitStateTransitionManager = sampleUnitStateTransitionManager;
  }

  /**
   * Asynchronously publish sample units to SampleDelivery queue transitioning the {@link
   * SampleUnit} to {@link SampleUnitDTO.SampleUnitState#DELIVERED}
   *
   * @param sampleSummaryId the sample summary id for the sample units to publish
   * @param collectionExerciseId the collection exercise the sample units are associated with
   */
  @Transactional
  @Async
  public void publishSampleUnits(UUID sampleSummaryId, UUID collectionExerciseId) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId);
    if (SampleSummaryDTO.SampleState.ACTIVE != sampleSummary.getState()) {
      return;
    }
    List<SampleUnit> sampleUnits = getSampleUnits(sampleSummary);
    transitionSampleUnitStateFromDeliveryEvent(sampleUnits);
    publishSampleUnits(sampleUnits, collectionExerciseId);
  }

  private List<SampleUnit> getSampleUnits(SampleSummary sampleSummary) {
    SampleUnit sampleUnit =
        SampleUnit.builder()
            .sampleSummaryFK(sampleSummary.getSampleSummaryPK())
            .state(SampleUnitDTO.SampleUnitState.PERSISTED)
            .build();
    Example<SampleUnit> example = Example.of(sampleUnit, ExampleMatcher.matchingAll());
    return sampleUnitRepository.findAll(example);
  }

  private void transitionSampleUnitStateFromDeliveryEvent(List<SampleUnit> sampleUnits) {
    for (SampleUnit sampleUnit : sampleUnits) {
      sampleUnit.setState(deliveringTransition(sampleUnit));
    }
    sampleUnitRepository.save(sampleUnits);
  }

  private SampleUnitDTO.SampleUnitState deliveringTransition(SampleUnit sampleUnit) {
    try {
      return sampleUnitStateTransitionManager.transition(
          sampleUnit.getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
    } catch (CTPException e) {
      log.warn("Could not transition to DELIVERING sampleunitpk={}", sampleUnit.getSampleUnitPK());
      return sampleUnit.getState();
    }
  }

  private void publishSampleUnits(List<SampleUnit> sampleUnits, UUID collectionExerciseId) {
    for (SampleUnit sampleUnit : sampleUnits) {
      try {
        sampleUnitPublisher.send(mapSampleUnit(collectionExerciseId, sampleUnit));
      } catch (AmqpException e) {
        log.error("Failed to publish sampleUnit={}", sampleUnit.getSampleUnitPK());
      }
    }
  }

  private uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mapSampleUnit(
      UUID collectionExerciseId, SampleUnit sampleUnit) {
    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
        mapperFacade.map(
            sampleUnit, uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);
    uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes =
        sampleAttributesRepository.findOne(sampleUnit.getId());
    mappedSampleUnit.setSampleAttributes(mapSampleAttributes(sampleAttributes));
    mappedSampleUnit.setCollectionExerciseId(collectionExerciseId.toString());
    return mappedSampleUnit;
  }

  private SampleAttributes mapSampleAttributes(
      uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes) {
    if (sampleAttributes == null) {
      return null;
    }
    SampleAttributes.Builder<Void> builder = new SampleAttributes().newCopyBuilder();
    sampleAttributes
        .getAttributes()
        .forEach((key, value) -> builder.addEntries().withKey(key).withValue(value));
    return builder.build();
  }
}
