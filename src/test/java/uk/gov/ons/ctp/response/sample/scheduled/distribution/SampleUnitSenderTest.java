package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

@RunWith(MockitoJUnitRunner.class)
public class SampleUnitSenderTest {
  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private SampleUnitPublisher sampleUnitPublisher;

  @Mock
  private StateTransitionManager<SampleUnitState, SampleUnitEvent> sampleUnitStateTransitionManager;

  @InjectMocks SampleUnitSender sampleUnitSender;

  @Test
  public void testSendSampleUnitSuccess() throws CTPException {
    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setState(SampleUnitState.PERSISTED);

    uk.gov.ons.ctp.response.libs.SampleUnit mappedSampleUnit =
        new uk.gov.ons.ctp.response.libs.SampleUnit();
    mappedSampleUnit.setId(UUID.randomUUID().toString());

    when(sampleUnitRepository.findById(any())).thenReturn(sampleUnit);
    when(sampleUnitStateTransitionManager.transition(any(), any()))
        .thenReturn(SampleUnitState.DELIVERED);

    sampleUnitSender.sendSampleUnit(mappedSampleUnit);

    verify(sampleUnitPublisher).send(mappedSampleUnit);
    verify(sampleUnitStateTransitionManager)
        .transition(SampleUnitState.PERSISTED, SampleUnitDTO.SampleUnitEvent.DELIVERING);

    ArgumentCaptor<SampleUnit> sampleUnitArgumentCaptor = ArgumentCaptor.forClass(SampleUnit.class);
    verify(sampleUnitRepository).saveAndFlush(sampleUnitArgumentCaptor.capture());
    assertEquals(sampleUnit, sampleUnitArgumentCaptor.getValue());
    assertEquals(SampleUnitState.DELIVERED, sampleUnitArgumentCaptor.getValue().getState());
  }
}
