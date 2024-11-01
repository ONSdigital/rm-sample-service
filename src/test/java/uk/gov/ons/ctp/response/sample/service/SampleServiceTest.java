package uk.gov.ons.ctp.response.sample.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import libs.common.FixtureHelper;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import libs.sample.validation.BusinessSampleUnit;
import org.assertj.core.api.Fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.client.CollectionExerciseSvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryLoadingStatus;
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

  @Mock private CollectionExerciseSvcClient collectionExerciseSvcClient;

  @InjectMocks private SampleService sampleService;

  private List<BusinessSurveySample> surveySample;

  private List<SampleUnit> sampleUnit;
  private List<SampleSummary> sampleSummaryList;

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
   * Test that when a sample unit is in the correct state
   *
   * @throws Exception oops
   */
  @Test
  public void updateStateTest() throws Exception {
    when(sampleSvcUnitStateTransitionManager.transition(
            SampleUnitState.INIT, SampleUnitEvent.PERSISTING))
        .thenReturn(SampleUnitState.PERSISTED);

    sampleService.updateState(sampleUnit.get(0));
    assertThat(sampleUnit.get(0).getState(), is(SampleUnitState.PERSISTED));
  }

  /**
   * Test that a sample summary is activated when all sample units are created.
   *
   * @throws Exception oops
   */
  @Test
  public void activateSampleSummaryTest() throws Exception {
    SampleSummary newSummary = createSampleSummary(1, 1);
    newSummary.setTotalSampleUnits(1);
    newSummary.setCollectionExerciseId(UUID.randomUUID());
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));
    when(sampleUnitRepository.countBySampleSummaryFKAndState(1, SampleUnitState.PERSISTED))
        .thenReturn(1);
    when(sampleSvcStateTransitionManager.transition(SampleState.INIT, SampleEvent.ACTIVATED))
        .thenReturn(SampleState.ACTIVE);

    SampleSummaryLoadingStatus output = sampleService.sampleSummaryStateCheck(UUID.randomUUID());
    assertEquals(output.getExpectedTotal().intValue(), 1);
    assertEquals(output.getCurrentTotal().intValue(), 1);
    assertTrue(output.isAreAllSampleUnitsLoaded());
  }

  private SampleSummary createSampleSummary(int numSamples, int expectedInstruments) {
    SampleSummary newSummary = sampleService.createAndSaveSampleSummary();

    newSummary.setTotalSampleUnits(numSamples);
    newSummary.setExpectedCollectionInstruments(expectedInstruments);
    newSummary.setSampleSummaryPK(1);
    return newSummary;
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
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

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
  public void deleteSampleSummarySuccess() {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setId(UUID.randomUUID());
    sampleService.deleteSampleSummaryAndSampleUnits(sampleSummary);
    verify(sampleUnitRepository, times(1)).deleteBySampleSummaryFK(any());
    verify(sampleSummaryRepository, times(1)).deleteByIdEquals(any());
  }

  @Test
  public void deleteSampleSummaryNotCalledWhenSampleUnitFails() {
    doThrow(new PersistenceException()).when(sampleUnitRepository).deleteBySampleSummaryFK(any());
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setId(UUID.randomUUID());
    try {
      sampleService.deleteSampleSummaryAndSampleUnits(sampleSummary);
      // Will only get here if the above method doesn't throw an exception
      Fail.fail("Exception should've been thrown");

    } catch (PersistenceException e) {
      verify(sampleUnitRepository, times(1)).deleteBySampleSummaryFK(any());
      verify(sampleSummaryRepository, never()).deleteByIdEquals(any());
    }
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
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
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

  @Test
  public void findSampleUnitsBySampleSummaryAndState() {
    SampleSummary newSummary = createSampleSummary(5, 2);
    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.of(newSummary));

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(UUID.randomUUID());
    String sampleUnitRef = "11111111";
    sampleUnit.setSampleUnitRef(sampleUnitRef);
    sampleUnit.setSampleUnitType("B");
    sampleUnit.setState(SampleUnitState.FAILED);

    List<SampleUnit> sampleUnits = new ArrayList<>();
    sampleUnits.add(sampleUnit);

    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            newSummary.getSampleSummaryPK(), SampleUnitState.FAILED))
        .thenReturn(sampleUnits.stream());

    List<SampleUnit> su =
        sampleService
            .findSampleUnitsBySampleSummaryAndState(newSummary.getId(), SampleUnitState.FAILED)
            .collect(Collectors.toList());

    assertEquals(sampleUnit, su.get(0));

    verify(sampleUnitRepository, times(1))
        .findBySampleSummaryFKAndState(newSummary.getSampleSummaryPK(), SampleUnitState.FAILED);
  }

  @Test
  public void findSampleUnitsBySampleSummaryAndStateReturnsEmptyList() {

    when(sampleSummaryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    List<SampleUnit> su =
        sampleService
            .findSampleUnitsBySampleSummaryAndState(UUID.randomUUID(), SampleUnitState.FAILED)
            .collect(Collectors.toList());
    assertTrue(su.isEmpty());

    verify(sampleUnitRepository, never()).findBySampleSummaryFKAndState(any(), any());
  }
}
