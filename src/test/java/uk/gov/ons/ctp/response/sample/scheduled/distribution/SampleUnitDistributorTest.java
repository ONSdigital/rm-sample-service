package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.SampleBeanMapper;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.SampleUnitDistribution;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  private DistributedListManager<Integer> sampleUnitDistributionListManager;

  @Mock
  private SampleUnitRepository sampleUnitRepo;

  @Mock
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Mock
  private SampleUnitPublisher sampleUnitPublisher;

  @Mock
  private SampleAttributesRepository sampleAttributesRepository;

  @Spy
  private MapperFacade mapperFacade = new SampleBeanMapper();

  @Mock
  private StateTransitionManager<SampleUnitState, SampleUnitEvent> sampleUnitStateTransitionManager;

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
    when(collectionExerciseJobRepository.findAll()).thenReturn(collectionExerciseJobsList);

    when(sampleUnitRepo.getSampleUnits(any(UUID.class), any(String.class),
            any(Integer.class), any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(2, info.getSampleUnitsSucceeded());

    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).saveList(any(String.class),
            any(List.class), any(Boolean.class));
    verify(sampleUnitStateTransitionManager, times(2)).transition(
            SampleUnitState.INIT, SampleUnitEvent.DELIVERING);
    verify(sampleUnitRepo, times(2)).saveAndFlush(any(SampleUnit.class));

    verify(sampleUnitPublisher, times(2)).send(
            any(uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class));
    verify(sampleUnitDistributionListManager, times(1)).deleteList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).unlockContainer();
  }

  @Test
  public void testFailRetrievingCollectionExerciseJob() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenThrow(new RuntimeException("Database access failed"));

    when(sampleUnitRepo.getSampleUnits(any(UUID.class), any(String.class),
            any(Integer.class), any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(0)).findList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(0)).saveList(any(String.class),
            any(List.class), any(Boolean.class));
  }

  @Test
  public void testRetrievingZeroCollectionExerciseJob() throws LockingException, CTPException {

    List<CollectionExerciseJob> collectionExerciseJobs = new ArrayList<>();

    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobs);

    when(sampleUnitRepo.getSampleUnits(any(UUID.class),  any(String.class),
            any(Integer.class),
            any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(collectionExerciseJobRepository, times(1)).findAll();

  }

  @Test
  public void testRetrievingCollectionExerciseJobNoSampleUnits() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    List<SampleUnit> sampleUnits = new ArrayList<>();

    when(sampleUnitRepo.getSampleUnits(any(UUID.class), any(String.class),
            any(Integer.class),
            any(List.class))).thenReturn(sampleUnits);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class),
            any(Boolean.class));

    verify(sampleUnitDistributionListManager, times(1)).deleteList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).unlockContainer();
  }

  @Test
  public void testRetrievingCollectionExerciseJobAndFailSampleUnits() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    when(sampleUnitRepo.getSampleUnits(any(UUID.class), any(String.class),
            any(Integer.class),
            any(List.class))).thenThrow(new RuntimeException("Database access failed"));

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(0, info.getSampleUnitsSucceeded());

    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class),
            any(Boolean.class));
  }

  @Test
  public void testRetrievingCollectionExerciseJobAndSampleUnits() throws LockingException, CTPException {
    when(collectionExerciseJobRepository.findAll())
            .thenReturn(collectionExerciseJobsList);

    when(sampleUnitRepo.getSampleUnits(any(UUID.class),any(String.class),
            any(Integer.class),
            any(List.class))).thenReturn(sampleUnitList);

    when(sampleUnitRepo.findOne(any(Integer.class))).thenReturn(sampleUnitList.get(0));

    SampleUnitDistributionInfo info = sampleUnitDistributor.distribute();
    assertEquals(0, info.getSampleUnitsFailed());
    assertEquals(2, info.getSampleUnitsSucceeded());

    verify(collectionExerciseJobRepository, times(1)).findAll();
    verify(sampleUnitDistributionListManager, times(1)).findList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).saveList(any(String.class),
            any(List.class), any(Boolean.class));
    verify(sampleUnitStateTransitionManager, times(2)).transition(
            SampleUnitState.INIT, SampleUnitEvent.DELIVERING);
    verify(sampleUnitRepo, times(2)).saveAndFlush(any(SampleUnit.class));

    verify(sampleUnitPublisher, times(2)).send(
            any(uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class));
    verify(sampleUnitDistributionListManager, times(1)).deleteList(any(String.class),
            any(Boolean.class));
    verify(sampleUnitDistributionListManager, times(1)).unlockContainer();
  }

  @Test
  public void testShouldSendSampleUnitWithAttributesWhenPresent() {
    // Given
    String addressKey = "Prem1";
    String addressValue = "14 ASHMEAD VIEW";
    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(UUID.randomUUID());
    collectionExerciseJob.setSampleSummaryId(UUID.randomUUID());
    given(collectionExerciseJobRepository.findAll()).willReturn(Collections.singletonList(collectionExerciseJob));

    SampleUnit sampleUnit = new SampleUnit();
    UUID sampleUnitId = UUID.randomUUID();
    sampleUnit.setId(sampleUnitId);
    SampleAttributes sampleAttributes = new SampleAttributes();
    sampleAttributes.setAttributes(Collections.singletonMap(addressKey, addressValue));
    given(sampleAttributesRepository.findOne(sampleUnit.getId())).willReturn(sampleAttributes);

    given(sampleUnitRepo.getSampleUnits(
            collectionExerciseJob.getSampleSummaryId(),
            SampleSummaryDTO.SampleState.ACTIVE.toString(),
            10,
            Collections.singletonList(-9999)
            )).willReturn(Collections.singletonList(sampleUnit));

    ArgumentCaptor<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> sampleUnitCaptor =
            ArgumentCaptor.forClass(uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);
    doNothing().when(this.sampleUnitPublisher).send(sampleUnitCaptor.capture());

    // When
    sampleUnitDistributor.distribute();

    // Then
    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit param = sampleUnitCaptor.getValue();
    assertEquals(collectionExerciseJob.getCollectionExerciseId().toString(), param.getCollectionExerciseId());
    assertEquals("Incorrect UUID for sample unit", sampleUnitId, UUID.fromString(param.getId()));

    List<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.SampleAttributes.Entry> entries =
            param.getSampleAttributes().getEntries();

    assertEquals("Incorrect number of sample attrbutes", 1, entries.size());

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.SampleAttributes.Entry entry = entries.get(0);

    assertEquals("Attribute key incorrect", addressKey, entry.getKey());
    assertEquals("Attribute value incorrect", addressValue, entry.getValue());
  }

  @Test
  public void testShouldSendSampleUnitWithoutAttributesWhenNotPresent() {
    // Given
    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(UUID.randomUUID());
    collectionExerciseJob.setSampleSummaryId(UUID.randomUUID());
    given(collectionExerciseJobRepository.findAll()).willReturn(Collections.singletonList(collectionExerciseJob));

    SampleUnit sampleUnit = new SampleUnit();
    UUID sampleUnitId = UUID.randomUUID();
    sampleUnit.setId(sampleUnitId);

    given(sampleUnitRepo.getSampleUnits(
            collectionExerciseJob.getSampleSummaryId(),
            SampleSummaryDTO.SampleState.ACTIVE.toString(),
            10,
            Collections.singletonList(-9999)
    )).willReturn(Collections.singletonList(sampleUnit));

    ArgumentCaptor<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> sampleUnitCaptor =
            ArgumentCaptor.forClass(uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);
    doNothing().when(this.sampleUnitPublisher).send(sampleUnitCaptor.capture());

    // When
    sampleUnitDistributor.distribute();

    // Then
    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit param = sampleUnitCaptor.getValue();
    assertEquals("Incorrect collection exercise", collectionExerciseJob.getCollectionExerciseId().toString(), param.getCollectionExerciseId());
    assertEquals("Incorrect UUID for sample unit", sampleUnitId, UUID.fromString(param.getId()));

    assertNull("Sample attributs should be null", param.getSampleAttributes());
  }
}
