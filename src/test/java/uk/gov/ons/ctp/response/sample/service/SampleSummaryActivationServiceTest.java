package uk.gov.ons.ctp.response.sample.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.message.SampleSummaryActivationStatusPublisher;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
          throws SampleSummaryActivationException, UnknownSampleSummaryException, NoSampleUnitsInSampleSummaryException {

    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID)).thenReturn(true);

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
    verify(sampleSummaryEnrichmentService, times(1)).enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
    verify(sampleSummaryDistributionService, times(1)).distribute(SAMPLE_SUMMARY_ID);
  }

  @Test(expected = SampleSummaryActivationException.class)
  public void testActivateSampleSummaryFromPubsubCorrectEnrichExceptionThrown()
          throws UnknownSampleSummaryException, SampleSummaryActivationException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    when(sampleSummaryEnrichmentService.enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID)).thenThrow(new UnknownSampleSummaryException());

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
    verify(sampleSummaryEnrichmentService, times(1)).enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
    verify(sampleSummaryActivationService, times(1)).sendEnrichStatusToCollectionExercise(COLLECTION_EXERCISE_ID, false);
    verify(sampleSummaryActivationService, never()).sendDistributeStatusToCollectionExercise(COLLECTION_EXERCISE_ID, anyBoolean());
  }

  @Test(expected = SampleSummaryActivationException.class)
  public void testActivateSampleSummaryFromPubsubCorrectDistributeExceptionThrown()
          throws UnknownSampleSummaryException, SampleSummaryActivationException, NoSampleUnitsInSampleSummaryException {
    SampleSummaryActivationDTO sampleSummaryActivation = new SampleSummaryActivationDTO();
    sampleSummaryActivation.setSampleSummaryId(SAMPLE_SUMMARY_ID);
    sampleSummaryActivation.setCollectionExerciseId(COLLECTION_EXERCISE_ID);
    sampleSummaryActivation.setSurveyId(SURVEY_ID);

    doThrow(new NoSampleUnitsInSampleSummaryException()).when(sampleSummaryDistributionService).distribute(SAMPLE_SUMMARY_ID);

    sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);
    verify(sampleSummaryEnrichmentService, times(1)).enrich(SURVEY_ID, SAMPLE_SUMMARY_ID, COLLECTION_EXERCISE_ID);
    verify(sampleSummaryActivationService, times(1)).sendEnrichStatusToCollectionExercise(COLLECTION_EXERCISE_ID, true);
    verify(sampleSummaryDistributionService, times(1)).distribute(SAMPLE_SUMMARY_ID);
    verify(sampleSummaryActivationService, times(1)).sendDistributeStatusToCollectionExercise(COLLECTION_EXERCISE_ID, false);
  }
}
