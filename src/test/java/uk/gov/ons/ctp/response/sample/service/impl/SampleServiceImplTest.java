package uk.gov.ons.ctp.response.sample.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterBusiness;
import uk.gov.ons.ctp.response.sample.message.EventPublisher;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.message.SampleOutboundPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;
import validation.BusinessSurveySample;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *tests
 */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {

  private static final String SAMPLE_SUMMARY_ID = "c6ea7ae3-468d-4b7d-847c-af54874baa46";

  private static final String SAMPLEUNIT_ID = "4ef7326b-4143-43f7-ba67-65056d4e20b8";

  @Mock
  private SampleSummaryRepository sampleSummaryRepository;

  @Mock
  private SampleUnitRepository sampleUnitRepository;
  
  @Mock
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Mock
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent> sampleSvcUnitStateTransitionManager;

  @Mock
  private PartySvcClientService partySvcClient;

  @Mock
  private PartyPublisher partyPublisher;

  @Mock
  private SampleOutboundPublisher sampleOutboundPublisher;

  @Mock
  private CollectionExerciseJobService collectionExerciseJobService;

  @Mock
  private EventPublisher eventPublisher;

  @Mock
  private CsvIngesterBusiness csvIngesterBusiness;

  @InjectMocks
  private SampleServiceImpl sampleServiceImpl;

  private List<BusinessSurveySample> surveySample;
  private List<PartyCreationRequestDTO> party;
  private List<PartyDTO> partyDTO;
  private List<SampleUnit> sampleUnit;
  private List<SampleSummary> sampleSummaryList;
  private List<CollectionExerciseJob> collectionExerciseJobs;

  /**
   * Before the test
   *
   * @throws Exception oops
   */
  @Before
  public void setUp() throws Exception {
    surveySample = FixtureHelper.loadClassFixtures(BusinessSurveySample[].class);
    party = FixtureHelper.loadClassFixtures(PartyCreationRequestDTO[].class);
    partyDTO = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    sampleUnit = FixtureHelper.loadClassFixtures(SampleUnit[].class);
    sampleSummaryList = FixtureHelper.loadClassFixtures(SampleSummary[].class);
    collectionExerciseJobs = FixtureHelper.loadClassFixtures(CollectionExerciseJob[].class);

    // This is required for pretty much all tests that create a sample summary
    when(this.sampleSummaryRepository.save(any(SampleSummary.class))).then(returnsFirstArg());
  }

  /**
   * Verify that a SampleSummary is correctly created when a SurveySample is
   * passed into the method.
   *
   * @throws Exception oops
   */
  @Test
  public void verifySampleSummaryCreatedCorrectly() {
    SampleSummary sampleSummary = sampleServiceImpl.createAndSaveSampleSummary();

    assertNotNull(sampleSummary.getIngestDateTime());
    assertTrue(sampleSummary.getState() == SampleSummaryDTO.SampleState.INIT);
    assertNotNull(sampleSummary.getId());

    verify(sampleSummaryRepository).save(sampleSummary);
  }

  /**
   * Verify that a SampleSummary containing two SampleUnits is created and then saved to the database. Also
   * verifies that both SampleUnits are saved to the database and then published to
   * the internal queue.
   *
   * @throws Exception oops
   */
  @Test
  public void testSampleSummaryAndSampleUnitsAreSavedAndThenSampleUnitsPublishedToQueue() {
    SampleSummary newSummary = sampleServiceImpl.createAndSaveSampleSummary();
    BusinessSurveySample businessSample = surveySample.get(0);

    sampleServiceImpl.processSampleSummary(newSummary, businessSample.getSampleUnits());

    verify(sampleSummaryRepository, times(2)).save(any(SampleSummary.class));
    verify(sampleUnitRepository, times(2)).save(any(SampleUnit.class));
    verify(partyPublisher, times(2)).publish(any(PartyCreationRequestDTO.class));
  }

  /**
   * Test that when a Party is posted to Party Service the appropriate states
   * are changed
   *
   * @throws Exception oops
   */
  @Test
  public void postPartyDTOToPartyServiceAndUpdateStatesTest() throws Exception {
    when(partySvcClient.postParty(any())).thenReturn(partyDTO.get(0));
    when(sampleUnitRepository.findById(UUID.fromString(SAMPLEUNIT_ID))).thenReturn(sampleUnit.get(0));
    when(sampleSvcUnitStateTransitionManager.transition(SampleUnitState.INIT, SampleUnitEvent.PERSISTING))
        .thenReturn(SampleUnitState.PERSISTED);
    when(sampleSummaryRepository.findOne(1)).thenReturn(sampleSummaryList.get(0));
    when(sampleSvcStateTransitionManager.transition(SampleState.INIT, SampleEvent.ACTIVATED))
        .thenReturn(SampleState.ACTIVE);

    PartyDTO testParty = sampleServiceImpl.sendToPartyService(party.get(0));
    assertEquals(SAMPLE_SUMMARY_ID, testParty.getSampleSummaryId());
    verify(partySvcClient).postParty(any(PartyCreationRequestDTO.class));
    assertThat(sampleUnit.get(0).getState(), is(SampleUnitState.PERSISTED));
    assertThat(sampleSummaryList.get(0).getState(), is(SampleState.ACTIVE));
  }

  /**
   * Test that SampleSummary state is not changed to active unless all Party
   * objects have been sent to the Party Service
   *
   * @throws Exception oops
   */
  @Test
  public void sendToPartyServiceTestNotAllSampleUnitsPosted() throws Exception {
    when(partySvcClient.postParty(any())).thenReturn(partyDTO.get(0));
    when(sampleUnitRepository.findById(UUID.fromString(SAMPLEUNIT_ID))).thenReturn(sampleUnit.get(0));
    when(sampleSvcUnitStateTransitionManager.transition(SampleUnitState.INIT, SampleUnitEvent.PERSISTING))
        .thenReturn(SampleUnitState.PERSISTED);
    when(sampleSummaryRepository.findOne(1)).thenReturn(sampleSummaryList.get(0));
    when(sampleSvcStateTransitionManager.transition(SampleState.INIT, SampleEvent.ACTIVATED))
        .thenReturn(SampleState.ACTIVE);
    when(sampleUnitRepository.getTotalForSampleSummary(1)).thenReturn(1);

    PartyDTO testParty = sampleServiceImpl.sendToPartyService(party.get(0));
    assertEquals(SAMPLE_SUMMARY_ID, testParty.getSampleSummaryId());
    verify(partySvcClient).postParty(any(PartyCreationRequestDTO.class));
    assertThat(sampleUnit.get(0).getState(), is(SampleUnitState.PERSISTED));
    assertThat(sampleSummaryList.get(0).getState(), not(SampleState.ACTIVE));
  }

  /**
   * Test that a CollectionExerciseJob is only stored if there are SampleUnits
   * found for the surveyRef and have not been previously sent
   *
   * @throws Exception oops
   */
  @Test
  public void testNoCollectionExerciseStoredWhenNoSampleUnits() throws Exception {
    Integer sampleUnitsTotal = sampleServiceImpl.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(0)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(0));
  }

  /**
   * Test that a CollectionExerciseJob is stored if there are SampleUnits
   * found for the surveyRef that have not been previously sent to CollectionExercise
   *
   * @throws Exception oops
   */
  @Test
  public void testOneCollectionExerciseJobIsStoredWhenSampleUnitsAreFound() throws Exception {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any())).thenReturn(newSummary);
    Integer sampleUnitsTotal = sampleServiceImpl.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(1)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(5));
  }

  private SampleSummary createSampleSummary(int numSamples, int expectedInstruments){
    SampleSummary newSummary = sampleServiceImpl.createAndSaveSampleSummary();

    newSummary.setTotalSampleUnits(numSamples);
    newSummary.setExpectedCollectionInstruments(expectedInstruments);

    return newSummary;
  }

  /**
   * Test that a CollectionExerciseJob is NOT stored if there are No SampleUnits
   * found for the surveyRef that have not been previously sent to CollectionExercise
   *
   * @throws Exception oops
   */
  @Test
  public void testNoCollectionExerciseJobIsStoredWhenNoSampleUnitsAreFound() throws Exception {
    SampleSummary newSummary = createSampleSummary(0, 2);
    when(sampleSummaryRepository.findById(any())).thenReturn(newSummary);
    Integer sampleUnitsTotal = sampleServiceImpl.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(0)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(0));
  }
  
  /**
   * Test that a CollectionExerciseJob is NOT stored if there are No SampleSummaries
   * found for the surveyRef that have not been previously sent to CollectionExercise
   *
   * @throws Exception oops
   */
  @Test
  public void testNoCollectionExerciseJobIsStoredWhenNoSampleSummaryIsFound() throws Exception {
	SampleSummary sampleSummary = null;
    when(sampleSummaryRepository.findById(any())).thenReturn(sampleSummary);
    Integer sampleUnitsTotal = sampleServiceImpl.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(0)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(0));
  }
  
  @Test
  public void testIngestBTypeSample() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile("file", "data".getBytes());

    // When
    Pair<SampleSummary, Future<Optional<SampleSummary>>> result = sampleServiceImpl.ingest(file, "B");

    // Forces wait for completion
    result.getRight().get();

    // Then
    verify(csvIngesterBusiness, times(1)).ingest(result.getLeft(), file);
  }

  @Test
  public void testIngestTypeSampleIsCaseInsensitive() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile("file", "data".getBytes());

    // When
    Pair<SampleSummary, Future<Optional<SampleSummary>>> result = sampleServiceImpl.ingest(file, "b");

    // Forces wait for completion
    result.getRight().get();

    // Then
    verify(csvIngesterBusiness, times(1)).ingest(result.getLeft(), file);
  }

  @Test
  public void testUploadInvalidTypeSample() throws Exception {
    when(this.sampleSvcStateTransitionManager.transition(SampleState.INIT, SampleEvent.FAIL_VALIDATION))
            .thenReturn(SampleState.FAILED);
    // Given
    MockMultipartFile file = new MockMultipartFile("file", "data".getBytes());

    // When
    Pair<SampleSummary, Future<Optional<SampleSummary>>> result = sampleServiceImpl.ingest(file, "invalid-type");

    // Forces wait for completion
    SampleSummary finalSummary = result.getRight().get().get();

    // Then expect exception
    assertEquals(SampleState.FAILED, finalSummary.getState());
    assertTrue(finalSummary.getNotes().contains("UnsupportedOperationException"));
  }

  @Test
  public void testIngestMessagesSent() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile("file", "data".getBytes());

    // When
    Pair<SampleSummary, Future<Optional<SampleSummary>>> result = sampleServiceImpl.ingest(file, "B");

    // Forces wait for completion
    result.getRight().get();

    // Then
    verify(sampleOutboundPublisher, times(1)).sampleUploadStarted(any());
  }

}
