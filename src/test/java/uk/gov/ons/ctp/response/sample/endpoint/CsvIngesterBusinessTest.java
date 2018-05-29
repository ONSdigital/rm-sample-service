package uk.gov.ons.ctp.response.sample.endpoint;

import javassist.tools.rmi.Sample;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterBusiness;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnit;
import validation.BusinessSurveySample;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the CsvIngester distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterBusinessTest {

  @Spy
  private AppConfig appConfig = new AppConfig();

  @InjectMocks
  private CsvIngesterBusiness csvIngester;

  @InjectMocks
  private SampleEndpoint sampleEndpoint;

  @Mock
  private SampleService sampleService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String SAMPLEUNITREF = "1234567890";
  private static final String SAMPLEUNITTYPE = "B";
  private static final String FORMTYPE1 = "0015";
  private static final String CHECKLETTER = "F";
  private static final String FROSIC92 = "50300";
  private static final String RUSIC92 = "50300";
  private static final String FROSIC2007 = "45320";
  private static final String RUSIC2007 = "45320";
  private static final String FROEMPMENT = "8478";
  private static final String FROTOVER = "801325";
  private static final String ENTREF = "9900000576";
  private static final String LEGALSTATUS = "1";
  private static final String ENTREPMKR = "E";
  private static final String REGION = "FE";
  private static final String BIRTHDATE = "01/09/1993";
  private static final String ENTNAME1 = "ENTNAME1_COMPANY1";
  private static final String ENTNAME2 = "ENTNAME2_COMPANY1";
  private static final String ENTNAME3 = "ENTNAME3_COMPANY1";
  private static final String RUNAME1 = "RUNAME1_COMPANY1";
  private static final String RUNAME2 = "RUNAME2_COMPANY1";
  private static final String RUNAME3 = "RUNAME3_COMPANY1";
  private static final String TRADSTYLE1 = "TOTAL UK ACTIVITY";
  private static final String TRADSTYLE2 = "TRADSTYLE2_COMPANY1";
  private static final String TRADSTYLE3 = "TRADSTYLE3_COMPANY1";
  private static final String SELTYPE = "C";
  private static final String INCLEXCL = "D";
  private static final String CELLNO = "7";
  private static final String FORMTYPE = "15";
  private static final String CURRENCY = "S";

  /**
   * take a named test file and create a copy of it - is because the ingester
   * will delete the source csv file after ingest
   *
   * @param fileName source file name
   * @return the newly created file
   * @throws Exception oops
   */
  private MockMultipartFile getTestFile(String fileName) throws Exception {
    Path csvFileLocation = Paths.get(getClass().getClassLoader().getResource("csv/" + fileName).toURI());
    MockMultipartFile multipartFile = new MockMultipartFile("file", fileName, "csv",
            Files.readAllBytes(csvFileLocation));
    return multipartFile;
  }

  private MockMultipartFile getTestFileFromString(String content) throws Exception {
    return new MockMultipartFile("file", "fileName", "csv",
            content.getBytes());
  }

  @Test
  public void testBlueSky() throws Exception {
    SampleSummary sampleSummary = new SampleSummary();
    csvIngester.ingest(sampleSummary, getTestFile("business-survey-sample.csv"));
    verify(sampleService, times(1)).processSampleSummary(eq(sampleSummary),
        anyListOf(BusinessSampleUnit.class));
  }

  @Test
  public void testMultipleLines() throws Exception {
    SampleSummary sampleSummary = new SampleSummary();
    csvIngester.ingest(sampleSummary, getTestFile("business-survey-sample-multiple.csv"));
    verify(sampleService, times(1)).processSampleSummary(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class));
  }

  @Test(expected = CTPException.class)
  public void testDate() throws Exception {
    SampleSummary sampleSummary = new SampleSummary();
    csvIngester.ingest(sampleSummary, getTestFile("business-survey-sample-date.csv"));
    verify(sampleService, times(0)).processSampleSummary(eq(sampleSummary),
        anyListOf(BusinessSampleUnit.class));
  }

  @Test(expected = CTPException.class)
  public void testMissingColumns() throws Exception {
    SampleSummary sampleSummary = new SampleSummary();
    csvIngester.ingest(sampleSummary, getTestFile("business-survey-sample-missing-columns.csv"));
    verify(sampleService, times(0)).processSampleSummary(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class));
    thrown.expect(CTPException.class);
  }

  @Test(expected = CTPException.class)
  public void testMissingFormTypeColumn() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();
    String missingFormType = "49900000001:F:50300:50300:45320:45320:8478:801325:" +
            "9900000576:1:E:FE:01/09/1993:ENTNAME1_COMPANY1:ENTNAME2_COMPANY1:" +
            ":RUNAME1_COMPANY1:RUNNAME2_COMPANY1::TOTAL UK ACTIVITY:::C:D:7::S";

    // When
    csvIngester.ingest(sampleSummary, getTestFileFromString(missingFormType));

    // Then
    verify(sampleService, times(0)).processSampleSummary(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class));
    thrown.expect(CTPException.class);
  }

  @Test(expected = CTPException.class)
  public void testMissingSampleUnitRefColumn() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();
    String missingSampleUnitRef = ":F:50300:50300:45320:45320:8478:801325:" +
            "9900000576:1:E:FE:01/09/1993:ENTNAME1_COMPANY1:ENTNAME2_COMPANY1:" +
            ":RUNAME1_COMPANY1:RUNNAME2_COMPANY1::TOTAL UK ACTIVITY:::C:D:7:15:S";

    // When
    csvIngester.ingest(sampleSummary, getTestFileFromString(missingSampleUnitRef));

    // Then
    verify(sampleService, times(0)).processSampleSummary(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class));
    thrown.expect(CTPException.class);
  }

  @Test(expected = CTPException.class)
  public void ensureNoDuplicateUnitRef() throws Exception {
    SampleSummary sampleSummary = new SampleSummary();
    MockMultipartFile f = getTestFile("business-survey-duplicate-unitrefs.csv");

    csvIngester.ingest(sampleSummary, f);
  }

}
