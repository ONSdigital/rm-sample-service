package uk.gov.ons.ctp.response.sample.service.state;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import uk.gov.ons.ctp.common.state.BasicStateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;

/**
 * This is the state transition manager factory for the samplesvc. It intended
 * that this will be refactored into a common framework class and that it
 * initialises each entities manager from database held transitions.
 */
@Named
public class SampleSvcStateTransitionManagerFactory implements StateTransitionManagerFactory {


  public static final String SAMPLE_ENTITY = "Sample";

  private Map<String, StateTransitionManager<?, ?>> managers;

  /**
   * Create and init the factory with concrete StateTransitionManagers for each
   * required entity
   */
  public SampleSvcStateTransitionManagerFactory() {
    managers = new HashMap<>();

    Map<SampleState, Map<SampleEvent, SampleState>> transitions = new HashMap<>();

    Map<SampleEvent, SampleState> transitionMapForSampledInit = new HashMap<>();
    transitionMapForSampledInit.put(SampleEvent.ACTIVATED, SampleState.ACTIVE);
    transitions.put(SampleState.INIT, transitionMapForSampledInit);

    StateTransitionManager<SampleState, SampleEvent> caseStateTransitionManager =
        new BasicStateTransitionManager<>(transitions);

    managers.put(SAMPLE_ENTITY, caseStateTransitionManager);

  }

  @SuppressWarnings("unchecked")
  @Override
  public StateTransitionManager<?, ?> getStateTransitionManager(String entity) {
    return managers.get(entity);
  }

}
