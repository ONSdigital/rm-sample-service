package uk.gov.ons.ctp.response.sample.endpoint;

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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnitVerify;
import validation.BusinessSurveySampleVerify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

/**
 * Test the CsvIngester distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterTest {

  @Spy
  private AppConfig appConfig = new AppConfig();

  @InjectMocks
  private CsvIngester csvIngester;

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

  private MockMvc mockMvc;

  private static final String SERVER_URL = "/samples/bres/fileupload";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
        .standaloneSetup(sampleEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();
  }

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

  @Test
  public void testBlueSky() throws Exception {
    csvIngester.ingest(getTestFile("business-survey-sample.csv"));
    verify(sampleService, times(1)).processSampleSummary(any(BusinessSurveySampleVerify.class),
        anyListOf(BusinessSampleUnitVerify.class));

    /*BusinessSampleUnitVerify businessSampleUnitVerify = samplingUnitList.get(0);
    assertEquals(businessSampleUnitVerify.getCheckletter(), CHECKLETTER);
    assertEquals(businessSampleUnitVerify.getFrosic92(), FROSIC92);
    assertEquals(businessSampleUnitVerify.getRusic92(), RUSIC92);
    assertEquals(businessSampleUnitVerify.getFrosic2007(), FROSIC2007);
    assertEquals(businessSampleUnitVerify.getRusic2007(), RUSIC2007);
    assertEquals(businessSampleUnitVerify.getFroempment(), FROEMPMENT);
    assertEquals(businessSampleUnitVerify.getFrotover(), FROTOVER);
    assertEquals(businessSampleUnitVerify.getEntref(), ENTREF);
    assertEquals(businessSampleUnitVerify.getLegalstatus(), LEGALSTATUS);
    assertEquals(businessSampleUnitVerify.getEntrepmkr(), ENTREPMKR);
    assertEquals(businessSampleUnitVerify.getRegion(), REGION);
    assertEquals(businessSampleUnitVerify.getBirthdate(), BIRTHDATE);
    assertEquals(businessSampleUnitVerify.getEntname1(), ENTNAME1);
    assertEquals(businessSampleUnitVerify.getEntname2(), ENTNAME2);
    assertEquals(businessSampleUnitVerify.getEntname3(), ENTNAME3);
    assertEquals(businessSampleUnitVerify.getRuname1(), RUNAME1);
    assertEquals(businessSampleUnitVerify.getRuname2(), RUNAME2);
    assertEquals(businessSampleUnitVerify.getRuname3(), RUNAME3);
    assertEquals(businessSampleUnitVerify.getTradstyle1(), TRADSTYLE1);
    assertEquals(businessSampleUnitVerify.getTradstyle2(), TRADSTYLE2);
    assertEquals(businessSampleUnitVerify.getTradstyle3(), TRADSTYLE3);
    assertEquals(businessSampleUnitVerify.getSeltype(), SELTYPE);
    assertEquals(businessSampleUnitVerify.getInclexcl(), INCLEXCL);
    assertEquals(businessSampleUnitVerify.getCell_no(), CELLNO);
    assertEquals(businessSampleUnitVerify.getFormtype(), FORMTYPE);
    assertEquals(businessSampleUnitVerify.getCurrency(), CURRENCY);*/
  }

  @Test
  public void correctCSV() throws Exception {
    MockMultipartFile mockMultipartFile = getTestFile("business-survey-sample.csv");

    ResultActions actions = mockMvc.perform(fileUpload(SERVER_URL).file(mockMultipartFile));

    actions.andExpect(status().is2xxSuccessful());
  }

  @Test
  public void incorrectCheckletter() throws Exception {
    MockMultipartFile mockMultipartFile = getTestFile("business-survey-sample-incorrect-checkletter.csv");

    System.out.println(getStringFromInputStream(mockMultipartFile.getInputStream()));

    ResultActions actions = mockMvc.perform(fileUpload(SERVER_URL).file(mockMultipartFile));

/*    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
        .andExpect(jsonPath("$.error.timestamp", isA(String.class)))
        .andExpect(jsonPath("$.error.message", is("Error ingesting file business-survey-sample-incorrect-checkletter.csv")));*/
  }

  @Test(expected = CTPException.class)
  public void incorrectData() throws Exception {
    csvIngester.ingest(getTestFile("business-survey-sample-incorrect-data.csv"));
    verify(sampleService, times(0)).processSampleSummary(any(BusinessSurveySampleVerify.class),
        anyListOf(BusinessSampleUnitVerify.class));
    /*List<BusinessSampleUnitVerify> samplingUnitList = csvIngester.ingest(getTestFile("business-survey-sample-incorrect-data.csv"));

    BusinessSampleUnitVerify businessSampleUnitVerify = samplingUnitList.get(0);
    assertEquals(businessSampleUnitVerify.getCheckletter(), CHECKLETTER);
    assertEquals(businessSampleUnitVerify.getFrosic92(), FROSIC92);
    assertEquals(businessSampleUnitVerify.getRusic92(), RUSIC92);
    assertEquals(businessSampleUnitVerify.getFrosic2007(), FROSIC2007);
    assertEquals(businessSampleUnitVerify.getRusic2007(), RUSIC2007);
    assertEquals(businessSampleUnitVerify.getFroempment(), FROEMPMENT);
    assertEquals(businessSampleUnitVerify.getFrotover(), FROTOVER);
    assertEquals(businessSampleUnitVerify.getEntref(), ENTREF);
    assertEquals(businessSampleUnitVerify.getLegalstatus(), LEGALSTATUS);
    assertEquals(businessSampleUnitVerify.getEntrepmkr(), ENTREPMKR);
    assertEquals(businessSampleUnitVerify.getRegion(), REGION);
    assertEquals(businessSampleUnitVerify.getBirthdate(), BIRTHDATE);
    assertEquals(businessSampleUnitVerify.getEntname1(), ENTNAME1);
    assertEquals(businessSampleUnitVerify.getEntname2(), ENTNAME2);
    assertEquals(businessSampleUnitVerify.getEntname3(), ENTNAME3);
    assertEquals(businessSampleUnitVerify.getRuname1(), RUNAME1);
    assertEquals(businessSampleUnitVerify.getRuname2(), RUNAME2);
    assertEquals(businessSampleUnitVerify.getRuname3(), RUNAME3);
    assertEquals(businessSampleUnitVerify.getTradstyle1(), TRADSTYLE1);
    assertEquals(businessSampleUnitVerify.getTradstyle2(), TRADSTYLE2);
    assertEquals(businessSampleUnitVerify.getTradstyle3(), TRADSTYLE3);
    assertEquals(businessSampleUnitVerify.getSeltype(), SELTYPE);
    assertEquals(businessSampleUnitVerify.getInclexcl(), INCLEXCL);
    assertEquals(businessSampleUnitVerify.getCell_no(), CELLNO);
    assertEquals(businessSampleUnitVerify.getFormtype(), FORMTYPE);
    assertEquals(businessSampleUnitVerify.getCurrency(), CURRENCY);*/

    //thrown.expect(Exception.class);
  }

  @Test(expected = CTPException.class)
  public void missingColumns() throws Exception {
    csvIngester.ingest(getTestFile("business-survey-sample-missing-columns.csv"));
    verify(sampleService, times(0)).processSampleSummary(any(BusinessSurveySampleVerify.class),
        anyListOf(BusinessSampleUnitVerify.class));
    thrown.expect(CTPException.class);
  }

  // convert InputStream to String
  private static String getStringFromInputStream(InputStream is) {

    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();

    String line;
    try {

      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return sb.toString();

  }

}
