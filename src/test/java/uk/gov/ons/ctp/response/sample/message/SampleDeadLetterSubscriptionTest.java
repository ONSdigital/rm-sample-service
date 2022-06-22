package uk.gov.ons.ctp.response.sample.message;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.UUID;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;

@RunWith(MockitoJUnitRunner.class)
public class SampleDeadLetterSubscriptionTest {

  private static final String SAMPLE_SUMMARY_ID = "fa622b71-f158-4d51-82dd-c3417e31e32c";

  @InjectMocks private SampleDeadLetterReceiver sampleDeadLetterReceiver;

  @Mock private SampleService sampleService;

  @Mock
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent>
      sampleSummaryTransitionManager;

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Test
  public void testProcessDeadLetterSample()
      throws CTPException, UnknownSampleSummaryException, RuntimeException {
    UUID sampleSummaryId = UUID.fromString(SAMPLE_SUMMARY_ID);
    SampleSummary sampleSummary =
        buildSampleSummary(sampleSummaryId, SampleSummaryDTO.SampleState.INIT);
    SampleSummary failedSampleSummary =
        buildSampleSummary(sampleSummaryId, SampleSummaryDTO.SampleState.FAILED);

    when(sampleService.findSampleSummary(sampleSummaryId)).thenReturn(sampleSummary);
    when(sampleSummaryTransitionManager.transition(
            SampleSummaryDTO.SampleState.INIT, SampleSummaryDTO.SampleEvent.FAIL_INGESTION))
        .thenReturn(SampleSummaryDTO.SampleState.FAILED);
    when(this.sampleSummaryRepository.save(sampleSummary)).thenReturn(failedSampleSummary);

    sampleDeadLetterReceiver.process(sampleSummaryId);

    assertEquals(sampleSummary.getState(), SampleSummaryDTO.SampleState.FAILED);
  }

  private SampleSummary buildSampleSummary(
      UUID sampleSummaryId, SampleSummaryDTO.SampleState sampleState) {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setState(sampleState);
    return sampleSummary;
  }
}
