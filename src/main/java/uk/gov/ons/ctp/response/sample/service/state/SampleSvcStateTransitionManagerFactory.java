package uk.gov.ons.ctp.response.sample.service.state;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.state.BasicStateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

/**
 * This is the state transition manager factory for the samplesvc. It intended
 * that this will be refactored into a common framework class and that it
 * initialises each entities manager from database held transitions.
 */
@Component
public class SampleSvcStateTransitionManagerFactory implements StateTransitionManagerFactory {


  public static final String SAMPLE_ENTITY = "Sample";

  public static final String SAMPLE_UNIT_ENTITY = "SampleUnit";

  private Map<String, StateTransitionManager<?, ?>> managers;

  /**
   * Create and init the factory with concrete StateTransitionManagers for each
   * required entity
   */
  public SampleSvcStateTransitionManagerFactory() {
    managers = new HashMap<>();

    StateTransitionManager<SampleState, SampleEvent> sampleSummaryStateTransitionManager =
            createSampleSummaryStateTransitionManager();
    managers.put(SAMPLE_ENTITY, sampleSummaryStateTransitionManager);

    StateTransitionManager<SampleUnitState, SampleUnitEvent> sampleUnitStateTransitionManager =
            createSampleUnitStateTransitionManager();
    managers.put(SAMPLE_UNIT_ENTITY, sampleUnitStateTransitionManager);
  }

  /**
   * Create and init the factory with concrete StateTransitionManagers for each
   * required entity
   * @return StateTransitionManager
   */
  private StateTransitionManager<SampleState, SampleEvent> createSampleSummaryStateTransitionManager() {
    Map<SampleState, Map<SampleEvent, SampleState>> transitions = new HashMap<>();

    Map<SampleEvent, SampleState> transitionMapForSampledInit = new HashMap<>();
    transitionMapForSampledInit.put(SampleEvent.ACTIVATED, SampleState.ACTIVE);
    transitionMapForSampledInit.put(SampleEvent.FAIL_VALIDATION, SampleState.FAILED);
    transitions.put(SampleState.INIT, transitionMapForSampledInit);

    StateTransitionManager<SampleState, SampleEvent> stateTransitionManager =
            new BasicStateTransitionManager<>(transitions);
    return stateTransitionManager;
  }

  /**
   * Create and init the factory with concrete StateTransitionManagers for each
   * required entity
   * @return StateTransitionManager
   */
  private StateTransitionManager<SampleUnitState, SampleUnitEvent> createSampleUnitStateTransitionManager() {
    Map<SampleUnitState, Map<SampleUnitEvent, SampleUnitState>> transitions = new HashMap<>();

    Map<SampleUnitEvent, SampleUnitState> transitionMapForSampledPersisted = new HashMap<>();
    transitionMapForSampledPersisted.put(SampleUnitEvent.DELIVERING, SampleUnitState.DELIVERED);
    transitions.put(SampleUnitState.PERSISTED, transitionMapForSampledPersisted);

    Map<SampleUnitEvent, SampleUnitState> transitionMapForSampledInit = new HashMap<>();
    transitionMapForSampledInit.put(SampleUnitEvent.PERSISTING, SampleUnitState.PERSISTED);
    transitions.put(SampleUnitState.INIT, transitionMapForSampledInit);

    StateTransitionManager<SampleUnitState, SampleUnitEvent> stateTransitionManager =
            new BasicStateTransitionManager<>(transitions);
    return stateTransitionManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  public StateTransitionManager<?, ?> getStateTransitionManager(String entity) {
    return managers.get(entity);
  }

}
