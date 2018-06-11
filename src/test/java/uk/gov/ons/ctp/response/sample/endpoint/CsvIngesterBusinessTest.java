package uk.gov.ons.ctp.response.sample.endpoint;

import org.junit.Before;
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
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.TestFiles;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.SampleIngest;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterBusiness;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.ctp.response.sample.TestFiles.getTestFileFromString;

/**
 * Test the CsvIngester distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterBusinessTest {

  @Spy
  private AppConfig appConfig = new AppConfig();

  @InjectMocks
  private CsvIngesterBusiness csvIngester;

  @Mock
  private SampleService sampleService;

  @Mock
  private PartyPublisher partyPublisher;

  @Captor
  public ArgumentCaptor<List<BusinessSampleUnit>> argumentCaptor;

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

  private static final int TEST_MAX_ERRORS = 50;

  @Before
  public void setUp(){
    SampleIngest sampleIngest = new SampleIngest();
    sampleIngest.setMaxErrors(TEST_MAX_ERRORS);
    when(this.appConfig.getSampleIngest()).thenReturn(sampleIngest);
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
  public void testIngestBusinessSampleSuccess() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(UUID.randomUUID());
    BusinessSampleUnit businessSampleUnit = createBusinessSampleUnit();
    given(sampleService.saveSample(sampleSummary, Collections.singletonList(businessSampleUnit), SampleUnitState.INIT)).willReturn(sampleSummary);

    // When
    csvIngester.ingest(sampleSummary, TestFiles.getTestFile("business-survey-sample.csv"));

    // Then
    verify(sampleService, times(1)).saveSample(eq(sampleSummary),
        anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
  }

  @Test
  public void testParseMultipleLinesFromBusinessSampleFile() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(UUID.randomUUID());
    given(sampleService.saveSample(any(), any(), eq(SampleUnitState.INIT))).willReturn(sampleSummary);

    // When
    csvIngester.ingest(sampleSummary, TestFiles.getTestFile("business-survey-sample-multiple.csv"));

    // Then
    verify(sampleService, times(1)).saveSample(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
  }

  @Test(expected = CTPException.class)
  public void testInvalidDateFromSampleSummary() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();

    // When
    csvIngester.ingest(sampleSummary, TestFiles.getTestFile("business-survey-sample-date.csv"));

    // Then
    verify(sampleService, times(0)).saveSample(eq(sampleSummary),
        anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
  }

  @Test(expected = CTPException.class)
  public void testMissingColumns() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();

    // When
    csvIngester.ingest(sampleSummary, TestFiles.getTestFile("business-survey-sample-missing-columns.csv"));

    // Then
    verify(sampleService, times(0)).saveSample(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
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
    verify(sampleService, times(0)).saveSample(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
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
    verify(sampleService, times(0)).saveSample(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
    thrown.expect(CTPException.class);
  }

  @Test(expected = CTPException.class)
  public void ensureNoDuplicateUnitRef() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();
    MockMultipartFile f = TestFiles.getTestFile("business-survey-duplicate-unitrefs.csv");

    // When
    csvIngester.ingest(sampleSummary, f);

    // Then
    verify(sampleService, times(0)).saveSample(eq(sampleSummary),
            anyListOf(BusinessSampleUnit.class), eq(SampleUnitState.INIT));
    thrown.expect(CTPException.class);
  }

  @Test
  public void testPartyIsPublishedForValidSampleFile() throws Exception {
    // Given
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(UUID.randomUUID());
    given(sampleService.saveSample(any(), argumentCaptor.capture(), eq(SampleUnitState.INIT))).willReturn(sampleSummary);

    // When
    csvIngester.ingest(sampleSummary, TestFiles.getTestFile("business-survey-sample.csv"));

    // Then
    BusinessSampleUnit capturedBusinessSampleUnit = argumentCaptor.getValue().get(0);
    BusinessSampleUnit businessSampleUnit = createBusinessSampleUnit();
    businessSampleUnit.setSampleUnitId(capturedBusinessSampleUnit.getSampleUnitId());
    PartyCreationRequestDTO party = PartyUtil.convertToParty(businessSampleUnit);
    party.getAttributes().setSampleUnitId(businessSampleUnit.getSampleUnitId().toString());
    party.setSampleSummaryId(sampleSummary.getId().toString());
    verify(partyPublisher).publish(party);

  }

  private BusinessSampleUnit createBusinessSampleUnit() {
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();
    businessSampleUnit.setSampleUnitType("B");
    businessSampleUnit.setSampleUnitRef("49900000001");
    businessSampleUnit.setCheckletter("F");
    businessSampleUnit.setFrosic92("50300");
    businessSampleUnit.setRusic92("50300");
    businessSampleUnit.setFrosic2007("45320");
    businessSampleUnit.setRusic2007("45320");
    businessSampleUnit.setFroempment("8478");
    businessSampleUnit.setFrotover("801325");
    businessSampleUnit.setEntref("9900000576");
    businessSampleUnit.setLegalstatus("1");
    businessSampleUnit.setEntrepmkr("E");
    businessSampleUnit.setRegion("FE");
    businessSampleUnit.setBirthdate("01/09/1993");
    businessSampleUnit.setEntname1("ENTNAME1_COMPANY1");
    businessSampleUnit.setEntname2("ENTNAME2_COMPANY1");
    businessSampleUnit.setEntname3("");
    businessSampleUnit.setRuname1("RUNAME1_COMPANY1");
    businessSampleUnit.setRuname2("RUNNAME2_COMPANY1");
    businessSampleUnit.setRuname3("");
    businessSampleUnit.setTradstyle1("TOTAL UK ACTIVITY");
    businessSampleUnit.setTradstyle2("");
    businessSampleUnit.setTradstyle3("");
    businessSampleUnit.setSeltype("C");
    businessSampleUnit.setInclexcl("D");
    businessSampleUnit.setCell_no("7");
    businessSampleUnit.setFormType("15");
    businessSampleUnit.setCurrency("S");
    return businessSampleUnit;
  }

}
