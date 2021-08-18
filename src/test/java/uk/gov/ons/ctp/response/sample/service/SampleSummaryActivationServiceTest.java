package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;

/** tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryActivationServiceTest {

  private static final UUID SAMPLE_SUMMARY_ID = UUID.randomUUID();
  private static final UUID COLLECTION_EXERCISE_ID = UUID.randomUUID();
  private static final UUID SURVEY_ID = UUID.randomUUID();

  @Mock private SampleSummaryDistributionService sampleSummaryDistributionService;
  @Mock private SampleSummaryEnrichmentService sampleSummaryEnrichmentService;

  // class under test
  @InjectMocks private SampleSummaryActivationService sampleSummaryActivationService;

  @Test
  public void testActivateSampleSummaryFromPubsub()
      throws SampleSummaryActivationException, UnknownSampleSummaryException,
          NoSampleUnitsInSampleSummaryException {

    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(
            SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID))
        .thenReturn(true);

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
    verify(sampleSummaryEnrichmentService, times(1))
        .enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
    verify(sampleSummaryDistributionService, times(1)).distribute(SAMPLE_SUMMARY_ID);
  }

  @Test(expected = SampleSummaryActivationException.class)
  public void testActivateSampleSummaryFromPubsubCorrectEnrichExceptionThrown()
      throws UnknownSampleSummaryException, SampleSummaryActivationException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(
            SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID))
        .thenThrow(new UnknownSampleSummaryException());

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
    verify(sampleSummaryEnrichmentService, times(1))
        .enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
    verify(sampleSummaryActivationService, times(1))
        .sendEnrichStatusToCollectionExercise(COLLECTION_EXERCISE_ID, false);
    verify(sampleSummaryActivationService, never())
        .sendDistributeStatusToCollectionExercise(COLLECTION_EXERCISE_ID, anyBoolean());
  }

  @Test(expected = SampleSummaryActivationException.class)
  public void testActivateSampleSummaryFromPubsubCorrectDistributeExceptionThrown()
      throws UnknownSampleSummaryException, SampleSummaryActivationException,
          NoSampleUnitsInSampleSummaryException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    doThrow(new NoSampleUnitsInSampleSummaryException())
        .when(sampleSummaryDistributionService)
        .distribute(SAMPLE_SUMMARY_ID);

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
    verify(sampleSummaryEnrichmentService, times(1))
        .enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
    verify(sampleSummaryActivationService, times(1))
        .sendEnrichStatusToCollectionExercise(COLLECTION_EXERCISE_ID, true);
    verify(sampleSummaryDistributionService, times(1)).distribute(SAMPLE_SUMMARY_ID);
    verify(sampleSummaryActivationService, times(1))
        .sendDistributeStatusToCollectionExercise(COLLECTION_EXERCISE_ID, false);
  }
}
