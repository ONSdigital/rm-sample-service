package uk.gov.ons.ctp.response.sample.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import libs.common.FixtureHelper;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import libs.sample.validation.BusinessSampleUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.validation.BusinessSurveySample;

/** tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceTest {

  private static final String SAMPLE_SUMMARY_ID = "c6ea7ae3-468d-4b7d-847c-af54874baa46";

  private static final String SAMPLEUNIT_ID = "4ef7326b-4143-43f7-ba67-65056d4e20b8";

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent>
      sampleSvcStateTransitionManager;

  @Mock
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
      sampleSvcUnitStateTransitionManager;

  @Mock private CollectionExerciseJobService collectionExerciseJobService;

  @InjectMocks private SampleService sampleService;

  private List<BusinessSurveySample> surveySample;

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
    sampleUnit = FixtureHelper.loadClassFixtures(SampleUnit[].class);
    sampleSummaryList = FixtureHelper.loadClassFixtures(SampleSummary[].class);
    collectionExerciseJobs = FixtureHelper.loadClassFixtures(CollectionExerciseJob[].class);

    // This is required for pretty much all tests that create a sample summary
    when(this.sampleSummaryRepository.save(any(SampleSummary.class))).then(returnsFirstArg());
  }

  /**
   * Verify that a SampleSummary is correctly created when a SurveySample is passed into the method.
   */
  @Test
  public void verifySampleSummaryCreatedCorrectly() {
    SampleSummary sampleSummary = sampleService.createAndSaveSampleSummary();

    assertTrue(sampleSummary.getState() == SampleSummaryDTO.SampleState.INIT);
    assertNotNull(sampleSummary.getId());

    verify(sampleSummaryRepository).save(sampleSummary);
  }

  /**
   * Verify that a SampleSummary containing two SampleUnits is created and then saved to the
   * database. Also verifies that both SampleUnits are saved to the database and then published to
   * the internal queue.
   *
   * @throws Exception oops
   */
  @Test
  public void testSampleSummaryAndSampleUnitsAreSaved() {
    SampleSummary newSummary = sampleService.createAndSaveSampleSummary();
    BusinessSurveySample businessSample = surveySample.get(0);

    sampleService.saveSample(newSummary, businessSample.getSampleUnits(), SampleUnitState.INIT);

    verify(sampleSummaryRepository, times(2)).save(any(SampleSummary.class));
    verify(sampleUnitRepository, times(2)).save(any(SampleUnit.class));
  }

  /**
   * Test that when a Party is posted to Party Service the appropriate states are changed
   *
   * @throws Exception oops
   */
  @Test
  public void updateStatesTest() throws Exception {
    when(sampleSvcUnitStateTransitionManager.transition(
            SampleUnitState.INIT, SampleUnitEvent.PERSISTING))
        .thenReturn(SampleUnitState.PERSISTED);
    when(sampleSummaryRepository.findBySampleSummaryPK(1))
        .thenReturn(Optional.of(sampleSummaryList.get(0)));
    when(sampleSvcStateTransitionManager.transition(SampleState.INIT, SampleEvent.ACTIVATED))
        .thenReturn(SampleState.ACTIVE);

    sampleService.updateState(sampleUnit.get(0));
    assertThat(sampleUnit.get(0).getState(), is(SampleUnitState.PERSISTED));
    assertThat(sampleSummaryList.get(0).getState(), is(SampleState.ACTIVE));
  }

  /**
   * Test that a CollectionExerciseJob is only stored if there are SampleUnits found for the
   * surveyRef and have not been previously sent
   *
   * @throws Exception oops
   */
  @Test
  public void testNoCollectionExerciseStoredWhenNoSampleUnits() throws Exception {
    Integer sampleUnitsTotal =
        sampleService.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(0)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(0));
  }

  /**
   * Test that a CollectionExerciseJob is stored if there are SampleUnits found for the surveyRef
   * that have not been previously sent to CollectionExercise
   *
   * @throws Exception oops
   */
  @Test
  public void testOneCollectionExerciseJobIsStoredWhenSampleUnitsAreFound() throws Exception {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    Integer sampleUnitsTotal =
        sampleService.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(1)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(5));
  }

  private SampleSummary createSampleSummary(int numSamples, int expectedInstruments) {
    SampleSummary newSummary = sampleService.createAndSaveSampleSummary();

    newSummary.setTotalSampleUnits(numSamples);
    newSummary.setExpectedCollectionInstruments(expectedInstruments);
    newSummary.setSampleSummaryPK(1);
    when(this.sampleSummaryRepository.findBySampleSummaryPK(1)).thenReturn(Optional.of(newSummary));
    return newSummary;
  }

  /**
   * Test that a CollectionExerciseJob is NOT stored if there are No SampleUnits found for the
   * surveyRef that have not been previously sent to CollectionExercise
   *
   * @throws Exception oops
   */
  @Test
  public void testNoCollectionExerciseJobIsStoredWhenNoSampleUnitsAreFound() throws Exception {
    SampleSummary newSummary = createSampleSummary(0, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    Integer sampleUnitsTotal =
        sampleService.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(0)).storeCollectionExerciseJob(any());
    assertThat(sampleUnitsTotal, is(0));
  }

  /**
   * Test that a CollectionExerciseJob is NOT stored if there are No SampleSummaries found for the
   * surveyRef that have not been previously sent to CollectionExercise
   *
   * @throws Exception oops
   */
  @Test
  public void testNoCollectionExerciseJobIsStoredWhenNoSampleSummaryIsFound() throws Exception {
    SampleSummary sampleSummary = null;
    when(sampleSummaryRepository.findById(any(UUID.class)))
        .thenReturn(Optional.ofNullable(sampleSummary));
    Integer sampleUnitsTotal =
        sampleService.initialiseCollectionExerciseJob(collectionExerciseJobs.get(0));
    verify(collectionExerciseJobService, times(0)).storeCollectionExerciseJob(any());
    assertEquals(0, sampleUnitsTotal.intValue());
  }

  @Test
  public void getSampleSummaryUnitCountHappyPath() {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));

    int actualResult = sampleService.getSampleSummaryUnitCount(newSummary.getId());

    assertEquals(5, actualResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getSampleSummaryUnitCountSampleSummaryNonExistent() {
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(null));

    sampleService.getSampleSummaryUnitCount(UUID.randomUUID());
  }

  @Test(expected = IllegalStateException.class)
  public void getSampleSummaryUnitCountSampleSummaryNullSize() {
    SampleSummary newSummary = createSampleSummary(5, 2);
    newSummary.setTotalSampleUnits(null);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));

    sampleService.getSampleSummaryUnitCount(newSummary.getId());
  }

  @Test
  public void createSampleUnit() throws UnknownSampleSummaryException, CTPException {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    sampleService.createSampleUnit(newSummary.getId(), businessSampleUnit, SampleUnitState.INIT);
    verify(sampleUnitRepository, times(1)).save(any(SampleUnit.class));
  }

  @Test
  public void createDuplicateSampleUnitThrowsIllegalStateException()
      throws UnknownSampleSummaryException, CTPException {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    SampleUnit sampleUnit =
        sampleService.createSampleUnit(
            newSummary.getId(), businessSampleUnit, SampleUnitState.INIT);
    when(sampleUnitRepository.existsBySampleUnitRefAndSampleSummaryFK(
            businessSampleUnit.getSampleUnitRef(), newSummary.getSampleSummaryPK()))
        .thenReturn(true);

    try {
      sampleService.createSampleUnit(newSummary.getId(), businessSampleUnit, SampleUnitState.INIT);
      fail("second attempt to create the same sample should fail");
    } catch (IllegalStateException e) {
      // expected exception
    }
    // confirm it was only saved once
    verify(sampleUnitRepository, times(1)).save(any(SampleUnit.class));
  }

  @Test(expected = UnknownSampleSummaryException.class)
  public void createSampleUnitWithUnknownSampleSummary()
      throws UnknownSampleSummaryException, CTPException {
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    sampleService.createSampleUnit(UUID.randomUUID(), businessSampleUnit, SampleUnitState.INIT);
  }

  @Test
  public void findSampleUnitWithSampleSummary()
      throws UnknownSampleSummaryException, UnknownSampleUnitException {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    when(sampleUnitRepository.findBySampleUnitRefAndSampleSummaryFK(
            businessSampleUnit.getSampleUnitRef(), newSummary.getSampleSummaryPK()))
        .thenReturn(sampleUnit.get(0));
    SampleUnit unit =
        sampleService.findSampleUnitBySampleSummaryAndSampleUnitRef(
            newSummary.getId(), businessSampleUnit.getSampleUnitRef());
    assertNotNull(unit);
    verify(sampleUnitRepository, times(1))
        .findBySampleUnitRefAndSampleSummaryFK(
            businessSampleUnit.getSampleUnitRef(), newSummary.getSampleSummaryPK());
  }

  @Test(expected = UnknownSampleSummaryException.class)
  public void findSampleUnitWithUnknownSampleSummary()
      throws UnknownSampleSummaryException, UnknownSampleUnitException, CTPException {

    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(null));
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    sampleService.createSampleUnit(newSummary.getId(), businessSampleUnit, SampleUnitState.INIT);
    verify(sampleUnitRepository, times(1)).save(any(SampleUnit.class));

    sampleService.findSampleUnitBySampleSummaryAndSampleUnitRef(
        UUID.randomUUID(), businessSampleUnit.getSampleUnitRef());
  }

  @Test(expected = UnknownSampleUnitException.class)
  public void findSampleUnitWithUnknownSampleUnitRef()
      throws UnknownSampleSummaryException, UnknownSampleUnitException {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    when(sampleUnitRepository.findBySampleUnitRefAndSampleSummaryFK(
            businessSampleUnit.getSampleUnitRef(), newSummary.getSampleSummaryPK()))
        .thenReturn(null);
    sampleService.findSampleUnitBySampleSummaryAndSampleUnitRef(
        newSummary.getId(), businessSampleUnit.getSampleUnitRef());
  }
}
