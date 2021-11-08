package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import libs.common.error.CTPException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;

@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseEndServiceTest {

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @InjectMocks CollectionExerciseEndService collectionExerciseEndService;

  @Test
  public void testCollectionExerciseEnd() throws CTPException {
    UUID testCollectionExerciseId = UUID.randomUUID();
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(1);
    when(sampleSummaryRepository.findByCollectionExerciseId(testCollectionExerciseId))
        .thenReturn(Optional.of(sampleSummary));

    collectionExerciseEndService.collectionExerciseEnd(testCollectionExerciseId);

    verify(sampleSummaryRepository).findByCollectionExerciseId(testCollectionExerciseId);
    verify(sampleUnitRepository).deleteBySampleSummaryFK(1);
  }

  @Test(expected = CTPException.class)
  public void testCollectionExerciseEndFailSampleSummaryNotFound() throws CTPException {
    UUID testCollectionExerciseId = UUID.randomUUID();
    when(sampleSummaryRepository.findByCollectionExerciseId(testCollectionExerciseId))
        .thenReturn(Optional.empty());

    collectionExerciseEndService.collectionExerciseEnd(testCollectionExerciseId);

    verify(sampleSummaryRepository).findByCollectionExerciseId(testCollectionExerciseId);
  }
}
