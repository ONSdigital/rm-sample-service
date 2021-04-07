package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import libs.common.error.CTPException;
import libs.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

/** Test the Sample Unit Distributor */
@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

  @Mock private SampleUnitSender sampleUnitSender;

  @Mock private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitMapper sampleUnitMapper;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @InjectMocks private SampleUnitDistributor sampleUnitDistributor;

  @Test
  public void testDistributeSuccess() throws CTPException, SampleDistributionException {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(666);
    sampleSummary.setState(SampleState.ACTIVE);

    SampleUnit sampleUnit = new SampleUnit();

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
        new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit();

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleSummaryRepository.findSampleSummaryById(any(UUID.class))).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitMapper.mapSampleUnit(any(), any())).thenReturn(mappedSampleUnit);

    sampleUnitDistributor.distribute();

    verify(sampleUnitRepository).findBySampleSummaryFKAndState(666, SampleUnitState.PERSISTED);

    ArgumentCaptor<CollectionExerciseJob> collexJobArgCap =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(collexJobArgCap.capture());
    assertEquals(collectionExerciseJob, collexJobArgCap.getValue());
    assertEquals(true, collexJobArgCap.getValue().isJobComplete());

    verify(sampleUnitSender).sendSampleUnit(mappedSampleUnit);
  }

  @Test
  public void testDistributeFail() throws CTPException, SampleDistributionException {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID sampleUnitId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(666);
    sampleSummary.setState(SampleState.ACTIVE);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
        new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit();

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleSummaryRepository.findSampleSummaryById(any(UUID.class))).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitMapper.mapSampleUnit(any(), any())).thenReturn(mappedSampleUnit);
    doThrow(new CTPException(Fault.SYSTEM_ERROR)).when(sampleUnitSender).sendSampleUnit(any());

    exceptionRule.expect(SampleDistributionException.class);
    exceptionRule.expectMessage("Some samples have failed transition for collection exericse Job");

    sampleUnitDistributor.distribute();

    verify(sampleUnitSender).sendSampleUnit(mappedSampleUnit);
    verify(collectionExerciseJobRepository, never()).saveAndFlush(any());
  }

  @Test
  public void testDistributeNoJobs() throws InterruptedException, SampleDistributionException {
    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.emptyList());

    sampleUnitDistributor.distribute();

    verify(collectionExerciseJobRepository).findByJobCompleteIsFalse();
    verify(collectionExerciseJobRepository, never()).saveAndFlush(any());
  }

  @Test
  public void testDistributeNoSampleUnits() throws CTPException, SampleDistributionException {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(666);

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleSummaryRepository.findSampleSummaryById(any(UUID.class))).thenReturn(Optional.of(sampleSummary));

    sampleUnitDistributor.distribute();

    verify(sampleUnitSender, never()).sendSampleUnit(any());

    ArgumentCaptor<CollectionExerciseJob> argumentCaptor =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(argumentCaptor.capture());
    assertEquals(true, argumentCaptor.getValue().isJobComplete());
    assertEquals(collexID, argumentCaptor.getValue().getCollectionExerciseId());
  }

  @Test
  public void testDistributeSummaryFailed() throws CTPException, SampleDistributionException {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(666);
    sampleSummary.setState(SampleState.FAILED);

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleSummaryRepository.findSampleSummaryById(any(UUID.class))).thenReturn(Optional.of(sampleSummary));

    sampleUnitDistributor.distribute();

    verify(sampleUnitSender, never()).sendSampleUnit(any());

    ArgumentCaptor<CollectionExerciseJob> argumentCaptor =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(argumentCaptor.capture());
    assertEquals(true, argumentCaptor.getValue().isJobComplete());
    assertEquals(collexID, argumentCaptor.getValue().getCollectionExerciseId());
  }
}
