package uk.gov.ons.ctp.response.sample.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;

/** tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryDistributionServiceTest {

  private static final UUID SAMPLE_SUMMARY_ID = UUID.randomUUID();
  private static final String COLLECTION_EXERCISE_ID = "dd83d654-ed2d-4265-b554-a5eb579904b4";
  private static final String PARTY_ID = "f6135ff0-a3fa-4baf-a61d-08a350266189";
  private static final String SAMPLE_UNIT_ID = "4ef7326b-4143-43f7-ba67-65056d4e20b8";
  private static final String SAMPLE_UNIT_REF = "12345678901";
  private static final String SAMPLE_UNIT_TYPE = "B";

  @Mock private SampleSummaryRepository sampleSummaryRepository;
  @Mock private SampleUnitPublisher sampleUnitPublisher;
  @Mock private SampleService sampleService;

  // class under test
  @InjectMocks private SampleSummaryDistributionService sampleSummaryDistributionService;

  @Test
  public void testDistribute()
      throws UnknownSampleSummaryException, NoSampleUnitsInSampleSummaryException {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(SAMPLE_SUMMARY_ID);
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setCollectionExerciseId(UUID.fromString(COLLECTION_EXERCISE_ID));

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(UUID.fromString(SAMPLE_UNIT_ID));
    sampleUnit.setSampleUnitRef(SAMPLE_UNIT_REF);
    sampleUnit.setSampleUnitType(SAMPLE_UNIT_TYPE);
    sampleUnit.setPartyId(UUID.fromString(PARTY_ID));
    List<SampleUnit> samples = new ArrayList<>();
    samples.add(sampleUnit);

    when(sampleSummaryRepository.findById(SAMPLE_SUMMARY_ID))
        .thenReturn(Optional.of(sampleSummary));
    when(sampleService.findSampleUnitsBySampleSummary(SAMPLE_SUMMARY_ID)).thenReturn(samples);

    sampleSummaryDistributionService.distribute(SAMPLE_SUMMARY_ID);
    verify(sampleUnitPublisher, times(1)).sendSampleUnitToCase(any());
    verify(sampleSummaryRepository, times(1)).saveAndFlush(any());
  }

  @Test(expected = UnknownSampleSummaryException.class)
  public void testDistributeFailsWithUnknownSampleSummaryId()
      throws UnknownSampleSummaryException, NoSampleUnitsInSampleSummaryException {
    when(sampleSummaryRepository.findById(SAMPLE_SUMMARY_ID)).thenReturn(Optional.empty());
    sampleSummaryDistributionService.distribute(SAMPLE_SUMMARY_ID);
    verify(sampleSummaryRepository, never()).saveAndFlush(any());
  }

  @Test(expected = NoSampleUnitsInSampleSummaryException.class)
  public void testDistributeFailsWithNoSampleUnitsInSampleSummary()
      throws UnknownSampleSummaryException, NoSampleUnitsInSampleSummaryException {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(SAMPLE_SUMMARY_ID);
    sampleSummary.setSampleSummaryPK(1);
    when(sampleSummaryRepository.findById(SAMPLE_SUMMARY_ID))
        .thenReturn(Optional.of(sampleSummary));
    when(sampleService.findSampleUnitsBySampleSummary(SAMPLE_SUMMARY_ID))
        .thenReturn(new ArrayList<>());
    sampleSummaryDistributionService.distribute(SAMPLE_SUMMARY_ID);
    verify(sampleSummaryRepository, never()).saveAndFlush(any());
  }

  @Test
  public void testCreateSampleUnitParentDTOObject() {
    SampleUnit testSampleUnit = new SampleUnit();
    testSampleUnit.setId(UUID.fromString(SAMPLE_UNIT_ID));
    testSampleUnit.setActiveEnrolment(true);
    testSampleUnit.setSampleUnitType(SAMPLE_UNIT_TYPE);
    testSampleUnit.setSampleUnitRef(SAMPLE_UNIT_REF);
    testSampleUnit.setPartyId(UUID.fromString(PARTY_ID));

    SampleUnitParentDTO output =
        sampleSummaryDistributionService.createSampleUnitParentDTOObject(
            UUID.fromString(COLLECTION_EXERCISE_ID), testSampleUnit);

    assertEquals(output.getId(), SAMPLE_UNIT_ID);
    assertEquals(output.getSampleUnitType(), SAMPLE_UNIT_TYPE);
    assertEquals(output.getSampleUnitRef(), SAMPLE_UNIT_REF);
    assertTrue(output.isActiveEnrolment());
    assertEquals(output.getPartyId(), UUID.fromString(PARTY_ID));
    assertEquals(output.getCollectionExerciseId(), COLLECTION_EXERCISE_ID);
  }
}