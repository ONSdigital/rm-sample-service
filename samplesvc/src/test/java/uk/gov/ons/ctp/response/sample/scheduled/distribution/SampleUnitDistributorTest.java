package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.SampleBeanMapper;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.SampleUnitDistribution;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the Sample Unit Distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

  private static final int TEN = 10;

  private List<SampleUnit> sampleUnitList;

  private List<CollectionExerciseJob> collectionExerciseJobsList;

  @Spy
  private AppConfig appConfig = new AppConfig();

  @Mock
  private Tracer tracer;

  @Mock
  private DistributedListManager<Integer> sampleUnitDistributionListManager;

  @Mock
  private SampleUnitRepository sampleUnitRepo;

  @Mock
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Mock
  private SampleService sampleService;

  @Mock
  private SampleUnitPublisher sampleUnitPublisher;

  @Spy
  private MapperFacade mapperFacade = new SampleBeanMapper();

  @Mock
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent> sampleUnitStateTransitionManager;

  @InjectMocks
  private SampleUnitDistributor sampleUnitDistributor;

  @Before
  public void setUp() throws Exception {
    sampleUnitList = FixtureHelper.loadClassFixtures(SampleUnit[].class);
    collectionExerciseJobsList = FixtureHelper.loadClassFixtures(CollectionExerciseJob[].class);

    SampleUnitDistribution sampleUnitDistributionConfig = new SampleUnitDistribution();
    sampleUnitDistributionConfig.setDelayMilliSeconds(TEN);
    sampleUnitDistributionConfig.setRetrySleepSeconds(TEN);
    sampleUnitDistributionConfig.setRetrievalMax(TEN);
    appConfig.setSampleUnitDistribution(sampleUnitDistributionConfig);

    MockitoAnnotations.initMocks(this);

  }

  @Test
  public void testHappyPath() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    when(sampleUnitRepo.getSampleUnitBatch(any(String.class), any(Timestamp.class), any(String.class), any(Integer.class),
            any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(2, info.getSampleUnitsSucceeded());

    verify(tracer, times(1)).createSpan(any(String.class));
    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class), any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).saveList(any(String.class),
            any(List.class), any(Boolean.class));
    verify(sampleUnitRepo, times(2)).findOne(any(Integer.class));

    verify(sampleUnitStateTransitionManager, times(1)).transition(sampleUnitList.get(0).getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
    verify(sampleUnitRepo, times(2)).saveAndFlush(any(SampleUnit.class));

    verify(sampleUnitPublisher, times(2)).send(any(uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class));
    verify(sampleUnitDistributionListManager, times(1)).deleteList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).unlockContainer();
    verify(tracer, times(1)).close(any(Span.class));
  }

  @Test
  public void testFailRetrievingCollectionExerciseJob() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenThrow(new RuntimeException("Database access failed"));

    when(sampleUnitRepo.getSampleUnitBatch(any(String.class), any(Timestamp.class), any(String.class), any(Integer.class),
            any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(tracer, times(1)).createSpan(any(String.class));
    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(0)).findList(any(String.class), any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(0)).saveList(any(String.class),
            any(List.class), any(Boolean.class));
    verify(tracer, times(1)).close(any(Span.class));
  }

  @Test
  public void testRetrievingZeroCollectionExerciseJob() throws LockingException, CTPException {

    List<CollectionExerciseJob> collectionExerciseJobs = new ArrayList<>();

    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobs);

    when(sampleUnitRepo.getSampleUnitBatch(any(String.class), any(Timestamp.class), any(String.class), any(Integer.class),
            any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(tracer, times(1)).createSpan(any(String.class));
    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(tracer, times(1)).close(any(Span.class));

  }

  @Test
  public void testRetrievingCollectionExerciseJobNoSampleUnits() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    List<SampleUnit> sampleUnits = new ArrayList<>();

    when(sampleUnitRepo.getSampleUnitBatch(any(String.class), any(Timestamp.class), any(String.class), any(Integer.class),
            any(List.class))).thenReturn(sampleUnits);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(tracer, times(1)).createSpan(any(String.class));
    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class), any(Boolean.class));

    verify(sampleUnitDistributionListManager, times(1)).deleteList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).unlockContainer();
    verify(tracer, times(1)).close(any(Span.class));
  }

  @Test
  public void testRetrievingCollectionExerciseJobAndFailSampleUnits() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    when(sampleUnitRepo.getSampleUnitBatch(any(String.class), any(Timestamp.class), any(String.class), any(Integer.class),
            any(List.class))).thenThrow(new RuntimeException("Database access failed"));

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(tracer, times(1)).createSpan(any(String.class));
    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class), any(Boolean.class));
    verify(tracer, times(1)).close(any(Span.class));
  }

  @Test
  public void testRetrievingCollectionExerciseJobAndSampleUnits() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    when(sampleUnitRepo.getSampleUnitBatch(any(String.class), any(Timestamp.class), any(String.class), any(Integer.class),
            any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(2, info.getSampleUnitsSucceeded());

    verify(tracer, times(1)).createSpan(any(String.class));
    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class), any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).saveList(any(String.class),
            any(List.class), any(Boolean.class));
    verify(sampleUnitRepo, times(2)).findOne(any(Integer.class));

    verify(sampleUnitStateTransitionManager, times(1)).transition(sampleUnitList.get(0).getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
    verify(sampleUnitRepo, times(2)).saveAndFlush(any(SampleUnit.class));

    verify(sampleUnitPublisher, times(2)).send(any(uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class));
    verify(sampleUnitDistributionListManager, times(1)).deleteList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).unlockContainer();
    verify(tracer, times(1)).close(any(Span.class));
  }

}
