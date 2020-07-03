package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import libs.common.error.CTPException;
import libs.common.error.CTPException.Fault;
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
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.DataGrid;
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
  @Mock private AppConfig appConfig;

  @Mock private SampleUnitSender sampleUnitSender;

  @Mock private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitMapper sampleUnitMapper;

  @Mock private RedissonClient redissonClient;

  @Mock private Supplier<Boolean> kubeCronEnabled;

  @InjectMocks private SampleUnitDistributor sampleUnitDistributor;

  private RLock lock;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DataGrid dataGrid = new DataGrid();
    dataGrid.setLockTimeToLiveSeconds(60);
    when(appConfig.getDataGrid()).thenReturn(dataGrid);

    lock = mock(RLock.class);
    when(redissonClient.getFairLock(any())).thenReturn(lock);
    when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
    when(kubeCronEnabled.get()).thenReturn(false);
  }

  @Test
  public void testDistributeSuccess() throws CTPException {
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
    when(sampleSummaryRepository.findById(any())).thenReturn(sampleSummary);
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitMapper.mapSampleUnit(any(), any())).thenReturn(mappedSampleUnit);

    sampleUnitDistributor.distributeJobs();

    verify(sampleUnitRepository).findBySampleSummaryFKAndState(666, SampleUnitState.PERSISTED);

    ArgumentCaptor<CollectionExerciseJob> collexJobArgCap =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(collexJobArgCap.capture());
    assertEquals(collectionExerciseJob, collexJobArgCap.getValue());
    assertEquals(true, collexJobArgCap.getValue().isJobComplete());

    verify(sampleUnitSender).sendSampleUnit(mappedSampleUnit);

    verify(lock).unlock();
  }

  @Test
  public void testDistributeFail() throws CTPException {
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
    when(sampleSummaryRepository.findById(any())).thenReturn(sampleSummary);
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitMapper.mapSampleUnit(any(), any())).thenReturn(mappedSampleUnit);
    doThrow(new CTPException(Fault.SYSTEM_ERROR)).when(sampleUnitSender).sendSampleUnit(any());

    sampleUnitDistributor.distributeJobs();

    verify(sampleUnitSender).sendSampleUnit(mappedSampleUnit);
    verify(collectionExerciseJobRepository, never()).saveAndFlush(any());
    verify(lock).unlock();
  }

  @Test
  public void testDistributeNoJobs() throws InterruptedException {
    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.emptyList());

    sampleUnitDistributor.distributeJobs();

    verify(collectionExerciseJobRepository).findByJobCompleteIsFalse();
    verify(redissonClient, never()).getFairLock(any());
    verify(lock, never()).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    verify(collectionExerciseJobRepository, never()).saveAndFlush(any());
    verify(lock, never()).unlock();
  }

  @Test
  public void testDistributeNoSampleUnits() throws CTPException {
    UUID collexID = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexID);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(666);

    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleSummaryRepository.findById(any())).thenReturn(sampleSummary);
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.empty());

    sampleUnitDistributor.distributeJobs();

    verify(sampleUnitSender, never()).sendSampleUnit(any());

    ArgumentCaptor<CollectionExerciseJob> argumentCaptor =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(argumentCaptor.capture());
    assertEquals(true, argumentCaptor.getValue().isJobComplete());
    assertEquals(collexID, argumentCaptor.getValue().getCollectionExerciseId());
    verify(lock).unlock();
  }

  @Test
  public void testDistributeSummaryFailed() throws CTPException {
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
    when(sampleSummaryRepository.findById(any())).thenReturn(sampleSummary);
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.empty());

    sampleUnitDistributor.distributeJobs();

    verify(sampleUnitSender, never()).sendSampleUnit(any());

    ArgumentCaptor<CollectionExerciseJob> argumentCaptor =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(argumentCaptor.capture());
    assertEquals(true, argumentCaptor.getValue().isJobComplete());
    assertEquals(collexID, argumentCaptor.getValue().getCollectionExerciseId());
    verify(lock).unlock();
  }

  @Test
  public void willProcessCollectionExerciseJobIfKubernetesCronIsEnabled() throws CTPException {
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

    when(kubeCronEnabled.get()).thenReturn(true);
    when(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .thenReturn(Collections.singletonList(collectionExerciseJob));
    when(sampleSummaryRepository.findById(any())).thenReturn(sampleSummary);
    when(sampleUnitRepository.findBySampleSummaryFKAndState(any(), any()))
        .thenReturn(Stream.of(sampleUnit));
    when(sampleUnitMapper.mapSampleUnit(any(), any())).thenReturn(mappedSampleUnit);

    sampleUnitDistributor.distributeJobs();

    verify(sampleUnitRepository).findBySampleSummaryFKAndState(666, SampleUnitState.PERSISTED);

    ArgumentCaptor<CollectionExerciseJob> collexJobArgCap =
        ArgumentCaptor.forClass(CollectionExerciseJob.class);
    verify(collectionExerciseJobRepository).saveAndFlush(collexJobArgCap.capture());
    assertEquals(collectionExerciseJob, collexJobArgCap.getValue());
    assertEquals(true, collexJobArgCap.getValue().isJobComplete());

    verify(sampleUnitSender).sendSampleUnit(mappedSampleUnit);

    verify(lock, never()).unlock();
  }
}
