package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

/** Test the Sample Unit Distributor */
@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

  @Mock private SampleUnitSenderFactory sampleUnitSenderFactory;

  @Mock private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private SampleUnitMapper sampleUnitMapper;

  @Mock private RedissonClient redissonClient;

  @InjectMocks private SampleUnitDistributor sampleUnitDistributor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    RLock lock = mock(RLock.class);
    when(redissonClient.getFairLock(any())).thenReturn(lock);
    when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
  }

  @Test
  public void testDistributeSuccess() throws InterruptedException {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID sampleUnitId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);

    SampleUnitSender sampleUnitSender = mock(SampleUnitSender.class);

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitSenderFactory.getNewSampleUnitSender(any())).thenReturn(sampleUnitSender);
    when(sampleUnitSender.call()).thenReturn(Boolean.TRUE);

    sampleUnitDistributor.distribute();

    verify(sampleUnitRepository)
        .findBySampleSummaryFKAndState(sampleSummaryId, SampleUnitState.PERSISTED);

    ArgumentCaptor<CollectionExerciseJob> collexJobArgCap =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(collexJobArgCap.capture());
    assertEquals(true, collexJobArgCap.getValue().isJobComplete());
    assertEquals(collexID, collexJobArgCap.getValue().getCollectionExerciseId());

    verify(sampleUnitSender).call();
  }

  @Test
  public void testDistributeFail() {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID sampleUnitId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);

    SampleUnitSender sampleUnitSender = mock(SampleUnitSender.class);

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitSenderFactory.getNewSampleUnitSender(any())).thenReturn(sampleUnitSender);
    when(sampleUnitSender.call()).thenReturn(Boolean.FALSE);

    sampleUnitDistributor.distribute();

    verify(sampleUnitRepository)
        .findBySampleSummaryFKAndState(sampleSummaryId, SampleUnitState.PERSISTED);

    verify(collectionExerciseJobRepository, never()).saveAndFlush(any());

    verify(sampleUnitSender).call();
  }

  @Test
  public void testDistributeNoJobs() {
    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.emptyList());

    sampleUnitDistributor.distribute();

    verify(collectionExerciseJobRepository).findByJobCompleteIsFalse();
    verify(collectionExerciseJobRepository, never()).saveAndFlush(any());
  }

  @Test
  public void testDistributeNoSampleUnits() {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleUnitSender sampleUnitSender = mock(SampleUnitSender.class);

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.empty());
    when(sampleUnitSenderFactory.getNewSampleUnitSender(any())).thenReturn(sampleUnitSender);
    when(sampleUnitSender.call()).thenReturn(Boolean.TRUE);

    sampleUnitDistributor.distribute();

    verify(sampleUnitRepository)
        .findBySampleSummaryFKAndState(sampleSummaryId, SampleUnitState.PERSISTED);

    ArgumentCaptor<CollectionExerciseJob> argumentCaptor =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(argumentCaptor.capture());
    assertEquals(true, argumentCaptor.getValue().isJobComplete());
    assertEquals(collexID, argumentCaptor.getValue().getCollectionExerciseId());
  }
}
