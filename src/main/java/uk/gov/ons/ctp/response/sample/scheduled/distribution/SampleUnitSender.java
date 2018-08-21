package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Sends SampleUnits via Rabbit queue and updates DB state */
@Component
public class SampleUnitSender {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitSender.class);

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
  public void sendSampleUnit(SampleUnit mappedSampleUnit) throws CTPException {
    uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit =
        sampleUnitRepository.findById(UUID.fromString(mappedSampleUnit.getId()));

    // Highly unlikely, but do a quick (and inexpensive) check to make sure we haven't already
    // delivered this Sample Unit - could happen if there's a problem with the locking/Redis
    // timing out and releasing the lock due to the delivery process taking a long time.
    if (sampleUnit.getState() == SampleUnitState.PERSISTED) {
      // Send to Rabbit queue
      sampleUnitPublisher.send(mappedSampleUnit);

      // Because we've now successfully queued the message we can change the state in the DB
      SampleUnitDTO.SampleUnitState newState =
          sampleUnitStateTransitionManager.transition(
              sampleUnit.getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
      sampleUnit.setState(newState);
      sampleUnitRepository.saveAndFlush(sampleUnit);
    }
  }
}
