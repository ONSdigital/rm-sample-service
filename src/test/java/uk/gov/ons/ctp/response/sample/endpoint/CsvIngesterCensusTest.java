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
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterCensus;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.CensusSampleUnit;
import validation.CensusSurveySample;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the CsvIngester distributor
 */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterCensusTest {

  @Spy
  private AppConfig appConfig = new AppConfig();

  @InjectMocks
  private CsvIngesterCensus csvIngester;

  @InjectMocks
  private SampleEndpoint sampleEndpoint;

  @Mock
  private SampleService sampleService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
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
    csvIngester.ingest(getTestFile("census-survey-sample.csv"));
    verify(sampleService, times(1)).processSampleSummary(any(CensusSurveySample.class),
        anyListOf(CensusSampleUnit.class));
  }

  @Test(expected = CTPException.class)
  public void missingColumns() throws Exception {
    csvIngester.ingest(getTestFile("business-survey-sample-missing-columns.csv"));
    verify(sampleService, times(0)).processSampleSummary(any(CensusSurveySample.class),
        anyListOf(CensusSampleUnit.class));
    thrown.expect(CTPException.class);
  }

  @Test(expected = CTPException.class)
  public void incorrectData() throws Exception {
    csvIngester.ingest(getTestFile("business-survey-sample-incorrect-data.csv"));
    verify(sampleService, times(0)).processSampleSummary(any(CensusSurveySample.class),
        anyListOf(CensusSampleUnit.class));
    thrown.expect(CTPException.class);
  }

}
