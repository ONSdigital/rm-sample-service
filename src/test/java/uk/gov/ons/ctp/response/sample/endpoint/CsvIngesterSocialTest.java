package uk.gov.ons.ctp.response.sample.endpoint;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.TestFiles;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterSocial;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SocialSampleUnit;

/** Test the CsvIngester distributor */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterSocialTest {

  @InjectMocks private CsvIngesterSocial csvIngester;

  @Mock private SampleService sampleService;

  @Mock private SampleAttributesRepository sampleAttributesRepository;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Captor public ArgumentCaptor<List<SocialSampleUnit>> argumentCaptor;

  @Test
  public void testIngestSocialSampleFile() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    String csv =
        "ADDRESS_LINE1,ADDRESS_LINE2,LOCALITY,TOWN_NAME,POSTCODE,COUNTRY,REFERENCE,TLA\n"
            + "14 HILL VIEW,,,STOCKTON-ON-TEES,T121AB,E,00001,LMS";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    verify(sampleService)
        .saveSample(
            eq(newSummary), anyListOf(SocialSampleUnit.class), eq(SampleUnitState.PERSISTED));
  }

  @Test(expected = CTPException.class)
  public void testMissingMandatoryColumns() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    String csv =
        "ADDRESS_LINE1,ADDRESS_LINE2,LOCALITY,TOWN_NAME,POSTCODE,COUNTRY,REFERENCE,TLA\n"
            + "14 HILL VIEW,,,STOCKTON-ON-TEES,T121AB,E,,LMS";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    verify(sampleService, times(0))
        .saveSample(
            eq(newSummary), anyListOf(SocialSampleUnit.class), eq(SampleUnitState.PERSISTED));
    thrown.expect(CTPException.class);
    thrown.expectMessage("Error in row [1] due to missing field(s) [REFERENCE]");
  }

  @Test(expected = CTPException.class)
  public void testMissingMandatoryHeaders() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    String csv =
        "ADDRESS_LINE1,LOCALITY,TOWN_NAME,POSTCODE,COUNTRY,TLA\n"
            + "14 HILL VIEW,,STOCKTON-ON-TEES,T121AB,E,LMS";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    verify(sampleService, times(0))
        .saveSample(
            eq(newSummary), anyListOf(SocialSampleUnit.class), eq(SampleUnitState.PERSISTED));
    thrown.expect(CTPException.class);
    thrown.expectMessage("Error in header row, missing required headers [REFERENCE]");
  }

  @Test
  public void testIngestSocialSampleSavesAttributes() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    given(sampleService.saveSample(any(), argumentCaptor.capture(), eq(SampleUnitState.PERSISTED)))
        .willReturn(newSummary);
    String csv =
        "ADDRESS_LINE1,ADDRESS_LINE2,ORGANISATION_NAME,LOCALITY,TOWN_NAME,POSTCODE,COUNTRY,REFERENCE,TLA,UPRN\n"
            + "14 HILL VIEW,,,FAKEVILL,STOCKTON-ON-TEES,T121AB,E,00001,LMS,123456";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    SocialSampleUnit socialSampleUnit = argumentCaptor.getValue().get(0);
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put("ADDRESS_LINE1", "14 HILL VIEW")
            .put("ADDRESS_LINE2", "")
            .put("ORGANISATION_NAME", "")
            .put("LOCALITY", "FAKEVILL")
            .put("TOWN_NAME", "STOCKTON-ON-TEES")
            .put("POSTCODE", "T121AB")
            .put("COUNTRY", "E")
            .put("REFERENCE", "00001")
            .put("TLA", "LMS")
            .put("UPRN", "123456")
            .build();
    SampleAttributes sampleAttriubutes =
        new SampleAttributes(socialSampleUnit.getSampleUnitId(), attributes);
    List<SampleAttributes> sampleAttributesList = Collections.singletonList(sampleAttriubutes);
    verify(sampleAttributesRepository).save(sampleAttributesList);
  }
}
