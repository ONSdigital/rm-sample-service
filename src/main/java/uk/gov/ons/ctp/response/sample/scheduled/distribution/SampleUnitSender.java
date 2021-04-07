package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.NoSuchElementException;
import java.util.UUID;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    String sampleUnitId = mappedSampleUnit.getId();
    try {
      uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit =
              sampleUnitRepository.findById(UUID.fromString(sampleUnitId)).orElseThrow();

      if (sampleUnit.getState() == SampleUnitState.PERSISTED) {
        log.debug("Publishing mapped sampleUnit", kv("MappedSampleUnit", mappedSampleUnit));
        sampleUnitPublisher.send(mappedSampleUnit);

        log.debug("Transitioning state of sampleUnit", kv("SampleUnit", sampleUnit),
                kv("from", sampleUnit.getState()), kv("to", SampleUnitDTO.SampleUnitEvent.DELIVERING));
        SampleUnitDTO.SampleUnitState newState =
                sampleUnitStateTransitionManager.transition(
                        sampleUnit.getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
        sampleUnit.setState(newState);
        sampleUnitRepository.saveAndFlush(sampleUnit);
      }
    } catch (NoSuchElementException e) {
      log.error("unable to find sample unit ", kv("sampleUnitId", sampleUnitId));
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND);
    }
  }
}
