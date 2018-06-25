package uk.gov.ons.ctp.response.sample.endpoint;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.TestFiles;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterSocial;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SocialSampleUnit;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the CsvIngester distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterSocialTest {

  @Spy
  private AppConfig appConfig = new AppConfig();

  @InjectMocks
  private CsvIngesterSocial csvIngester;

  @InjectMocks
  private SampleEndpoint sampleEndpoint;

  @Mock
  private SampleService sampleService;

  @Mock
  private SampleAttributesRepository sampleAttributesRepository;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Captor
  public ArgumentCaptor<List<SocialSampleUnit>> argumentCaptor;


  @Test
  public void testIngestSocialSampleFile() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    String csv = "Prem1,Prem2,Prem3,Prem4,District,PostTown,Postcode,CountryCode,Reference\n" +
            "14 ASHMEAD VIEW,,,,,STOCKTON-ON-TEES,TS184QG,E,LMS00001";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    verify(sampleService).saveSample(eq(newSummary),
        anyListOf(SocialSampleUnit.class), eq(SampleUnitState.PERSISTED));
  }

  @Test(expected = CTPException.class)
  public void testMissingMandatoryColumns() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    String csv = "Prem1,Prem2,Prem3,Prem4,District,PostTown,Postcode,CountryCode\n" +
            "14 ASHMEAD VIEW,,,,,STOCKTON-ON-TEES,TS184QG,E";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    verify(sampleService, times(0)).saveSample(eq(newSummary),
        anyListOf(SocialSampleUnit.class), eq(SampleUnitState.PERSISTED));
    thrown.expect(CTPException.class);
  }

  @Test(expected = CTPException.class)
  public void testMissingMandatoryHeaders() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    String csv = "Prem2,Prem3,Prem4,District,PostTown,Postcode,CountryCode,Reference\n" +
            ",,,,STOCKTON-ON-TEES,TS184QG,E,LMS00001";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    verify(sampleService, times(0)).saveSample(eq(newSummary),
            anyListOf(SocialSampleUnit.class), eq(SampleUnitState.PERSISTED));
    thrown.expect(CTPException.class);
    thrown.expectMessage("Error in header row, missing required headers Prem1");
  }

  @Test
  public void testIngestSocialSampleSavesAttributes() throws Exception {
    // Given
    SampleSummary newSummary = new SampleSummary();
    given(sampleService.saveSample(any(), argumentCaptor.capture(), eq(SampleUnitState.PERSISTED))).willReturn(newSummary);
    String csv = "Prem1,Prem2,Prem3,Prem4,District,PostTown,Postcode,CountryCode,Reference\n" +
            "14 ASHMEAD VIEW,,,,,STOCKTON-ON-TEES,TS184QG,E,LMS00001";

    // When
    csvIngester.ingest(newSummary, TestFiles.getTestFileFromString(csv));

    // Then
    SocialSampleUnit socialSampleUnit = argumentCaptor.getValue().get(0);
    Map<String, String> attributes = ImmutableMap.<String, String>builder()
            .put("Prem1", "14 ASHMEAD VIEW")
            .put("Prem2", "")
            .put("Prem3", "")
            .put("Prem4", "")
            .put("District", "")
            .put("PostTown", "STOCKTON-ON-TEES")
            .put("Postcode", "TS184QG")
            .put("CountryCode", "E")
            .put("Reference", "LMS00001")
            .build();
    SampleAttributes sampleAttriubutes = new SampleAttributes(socialSampleUnit.getSampleUnitId(), attributes);
    List<SampleAttributes> sampleAttributesList = Collections.singletonList(sampleAttriubutes);
    verify(sampleAttributesRepository).save(sampleAttributesList);
  }

}
