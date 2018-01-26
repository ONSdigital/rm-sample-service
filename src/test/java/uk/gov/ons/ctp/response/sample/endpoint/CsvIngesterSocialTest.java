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
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterSocial;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SocialSampleUnit;
import validation.SocialSurveySample;

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
public class CsvIngesterSocialTest {

  @Spy
  private AppConfig appConfig = new AppConfig();

  @InjectMocks
  private CsvIngesterSocial csvIngester;

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
    csvIngester.ingest(getTestFile("social-survey-sample.csv"));
    verify(sampleService, times(1)).processSampleSummary(any(SocialSurveySample.class),
        anyListOf(SocialSampleUnit.class), eq(1));
  }

  @Test(expected = CTPException.class)
  public void missingColumns() throws Exception {
    csvIngester.ingest(getTestFile("social-survey-sample-missing-columns.csv"));
    verify(sampleService, times(0)).processSampleSummary(any(SocialSurveySample.class),
        anyListOf(SocialSampleUnit.class), eq(1));
    thrown.expect(CTPException.class);
  }

}
