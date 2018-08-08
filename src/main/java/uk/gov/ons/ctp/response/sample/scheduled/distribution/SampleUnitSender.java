package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

@Slf4j
@Component(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SampleUnitSender implements Callable<Boolean> {
  private List<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> mappedSampleUnits;

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

  public void setMappedSampleUnits(
      List<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> mappedSampleUnits) {
    this.mappedSampleUnits = mappedSampleUnits;
  }

  @Override
  public Boolean call() {
    Boolean result = Boolean.TRUE;

    for (uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit :
        mappedSampleUnits) {
      try {
        executeTransaction(mappedSampleUnit);
      } catch (Exception e) {
        log.error("Error processing sample unit id: {}", mappedSampleUnit.getId(), e);

        // Any kind of issue will have been rolled back and we should indicate a failure so
        // this can be retried at some future point by the poller
        result = Boolean.FALSE;
      }
    }

    return result;
  }

  @Transactional
  protected void executeTransaction(
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
