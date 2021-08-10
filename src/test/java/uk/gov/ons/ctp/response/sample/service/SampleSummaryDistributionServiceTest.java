package uk.gov.ons.ctp.response.sample.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;

/** tests */
@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryDistributionServiceTest {

  private static final String SAMPLE_SUMMARY_ID = "c6ea7ae3-468d-4b7d-847c-af54874baa46";
  private static final String COLLECTION_EXERCISE_ID = "dd83d654-ed2d-4265-b554-a5eb579904b4";
  private static final String PARTY_ID = "f6135ff0-a3fa-4baf-a61d-08a350266189";
  private static final String SAMPLEUNIT_ID = "4ef7326b-4143-43f7-ba67-65056d4e20b8";

  @InjectMocks private SampleSummaryDistributionService sampleSummaryDistributionService;

  @Test
  public void testCreateSampleUnitParentDTOObject() {
    SampleUnit testSampleUnit = new SampleUnit();
    testSampleUnit.setId(UUID.fromString(SAMPLEUNIT_ID));
    testSampleUnit.setActiveEnrolment(true);
    testSampleUnit.setSampleUnitRef("12345678901");
    testSampleUnit.setPartyId(UUID.fromString(PARTY_ID));

    SampleUnitParentDTO output =
        sampleSummaryDistributionService.createSampleUnitParentDTOObject(
            UUID.fromString(COLLECTION_EXERCISE_ID), testSampleUnit);

    assertEquals(output.getId(), UUID.fromString(SAMPLEUNIT_ID));
    assertEquals(output.getSampleUnitRef(), "12345678901");
    assertTrue(output.isActiveEnrolment());
    assertEquals(output.getPartyId(), UUID.fromString(PARTY_ID));
    assertEquals(output.getCollectionExerciseId(), COLLECTION_EXERCISE_ID);
  }
}
