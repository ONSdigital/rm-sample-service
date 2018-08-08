package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

/** Sends SampleUnits via Rabbit queue and updates DB state */
@Slf4j
@Component
public class SampleUnitSender {
  private final SampleUnitRepository sampleUnitRepository;

  private final SampleUnitPublisher sampleUnitPublisher;

  private final StateTransitionManager<SampleUnitState, SampleUnitEvent>
      sampleUnitStateTransitionManager;

  public SampleUnitSender(
      SampleUnitRepository sampleUnitRepository,
      SampleUnitPublisher sampleUnitPublisher,
      @Qualifier("sampleUnitTransitionManager")
          StateTransitionManager<SampleUnitState, SampleUnitEvent>
              sampleUnitStateTransitionManager) {
    this.sampleUnitRepository = sampleUnitRepository;
    this.sampleUnitPublisher = sampleUnitPublisher;
    this.sampleUnitStateTransitionManager = sampleUnitStateTransitionManager;
  }

  /** Send a SampleUnit */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendSampleUnit(
      uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit)
      throws CTPException {
    // Send to Rabbit queue
    sampleUnitPublisher.send(mappedSampleUnit);

    // Because we've now successfully queued the message we can change the state in the DB
    SampleUnit sampleUnit =
        sampleUnitRepository.findById(UUID.fromString(mappedSampleUnit.getId()));
    SampleUnitDTO.SampleUnitState newState =
        sampleUnitStateTransitionManager.transition(
            sampleUnit.getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
    sampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(sampleUnit);
  }
}
