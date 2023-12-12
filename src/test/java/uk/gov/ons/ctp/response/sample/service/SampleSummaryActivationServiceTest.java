package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Mockito.*;

import java.util.UUID;
import org.assertj.core.api.Fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.message.SampleSummaryActivationStatusPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryStatusDTO;

/** tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryActivationServiceTest {

  private static final UUID SAMPLE_SUMMARY_ID = UUID.randomUUID();
  private static final UUID COLLECTION_EXERCISE_ID = UUID.randomUUID();
  private static final UUID SURVEY_ID = UUID.randomUUID();

  @Mock private SampleSummaryDistributionService sampleSummaryDistributionService;
  @Mock private SampleSummaryEnrichmentService sampleSummaryEnrichmentService;
  @Mock private SampleSummaryActivationStatusPublisher sampleSummaryActivationStatusPublisher;

  // class under test
  @InjectMocks private SampleSummaryActivationService sampleSummaryActivationService;

  @Test
  public void testActivateSampleSummaryFromPubsub()
      throws SampleSummaryActivationException,
          UnknownSampleSummaryException,
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
      throws UnknownSampleSummaryException,
          SampleSummaryActivationException,
          NoSampleUnitsInSampleSummaryException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(
            SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID))
        .thenThrow(new UnknownSampleSummaryException());

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
  }

  @Test()
  public void testActivateSampleSummaryFromPubsubCorrectNumberOfCallsOnEnrichFailure()
      throws UnknownSampleSummaryException, NoSampleUnitsInSampleSummaryException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(
            SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID))
        .thenThrow(new UnknownSampleSummaryException());
    try {
      sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);

      // Will only get here if the above method doesn't throw an exception
      Fail.fail("Exception should've been thrown");
    } catch (SampleSummaryActivationException e) {
      verify(sampleSummaryEnrichmentService, times(1))
          .enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
      verify(sampleSummaryDistributionService, never()).distribute(SAMPLE_SUMMARY_ID);
      SampleSummaryStatusDTO sampleSummaryStatusDTO = new SampleSummaryStatusDTO();
      sampleSummaryStatusDTO.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
      sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.ENRICHED);
      sampleSummaryStatusDTO.setSuccessful(false);
      verify(sampleSummaryActivationStatusPublisher, times(1))
          .sendSampleSummaryActivation(sampleSummaryStatusDTO);

      sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.DISTRIBUTED);
      verify(sampleSummaryActivationStatusPublisher, never())
          .sendSampleSummaryActivation(sampleSummaryStatusDTO);
    }
  }

  @Test(expected = SampleSummaryActivationException.class)
  public void testActivateSampleSummaryFromPubsubCorrectDistributeExceptionThrown()
      throws SampleSummaryActivationException,
          NoSampleUnitsInSampleSummaryException,
          UnknownSampleSummaryException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(
            SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID))
        .thenReturn(true);

    doThrow(new NoSampleUnitsInSampleSummaryException())
        .when(sampleSummaryDistributionService)
        .distribute(SAMPLE_SUMMARY_ID);

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
  }

  @Test()
  public void testActivateSampleSummaryFromPubsubCorrectNumberOfCallsOnDistributeFailure()
      throws UnknownSampleSummaryException, NoSampleUnitsInSampleSummaryException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(
            SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID))
        .thenReturn(true);

    doThrow(new NoSampleUnitsInSampleSummaryException())
        .when(sampleSummaryDistributionService)
        .distribute(SAMPLE_SUMMARY_ID);

    try {
      sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);

      // Will only get here if the above method doesn't throw an exception
      Fail.fail("Exception should've been thrown");
    } catch (SampleSummaryActivationException e) {
      verify(sampleSummaryEnrichmentService, times(1))
          .enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
      verify(sampleSummaryDistributionService, times(1)).distribute(SAMPLE_SUMMARY_ID);

      SampleSummaryStatusDTO sampleSummaryStatusDTO = new SampleSummaryStatusDTO();
      sampleSummaryStatusDTO.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
      sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.ENRICHED);
      sampleSummaryStatusDTO.setSuccessful(true);
      verify(sampleSummaryActivationStatusPublisher, times(1))
          .sendSampleSummaryActivation(sampleSummaryStatusDTO);

      sampleSummaryStatusDTO.setSuccessful(false);
      sampleSummaryStatusDTO.setEvent(SampleSummaryStatusDTO.Event.DISTRIBUTED);
      verify(sampleSummaryActivationStatusPublisher, times(1))
          .sendSampleSummaryActivation(sampleSummaryStatusDTO);
    }
  }
}
