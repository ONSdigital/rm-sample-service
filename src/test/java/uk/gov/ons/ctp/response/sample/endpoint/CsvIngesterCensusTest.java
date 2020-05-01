package uk.gov.ons.ctp.response.sample.endpoint;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.ons.ctp.response.libs.common.error.CTPException;
import uk.gov.ons.ctp.response.libs.sample.validation.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterCensus;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/** Test the CsvIngester distributor */
@RunWith(MockitoJUnitRunner.class)
public class CsvIngesterCensusTest {

  @Spy private AppConfig appConfig = new AppConfig();

  @InjectMocks private CsvIngesterCensus csvIngester;

  @InjectMocks private SampleEndpoint sampleEndpoint;

  @Mock private SampleService sampleService;

  @Spy
  private Validator csvIngestValidator = Validation.buildDefaultValidatorFactory().getValidator();

  @Rule public ExpectedException thrown = ExpectedException.none();

  /**
   * take a named test file and create a copy of it - is because the ingester will delete the source
   * csv file after ingest
   *
   * @param fileName source file name
   * @return the newly created file
   * @throws Exception oops
   */
  private MockMultipartFile getTestFile(String fileName) throws Exception {
    Path csvFileLocation =
        Paths.get(getClass().getClassLoader().getResource("csv/" + fileName).toURI());
    MockMultipartFile multipartFile =
        new MockMultipartFile("file", fileName, "csv", Files.readAllBytes(csvFileLocation));
    return multipartFile;
  }

  @Test
  public void testBlueSky() throws Exception {
    SampleSummary newSummary = new SampleSummary();
    csvIngester.ingest(newSummary, getTestFile("census-survey-sample.csv"));
    verify(sampleService, times(1))
        .saveSample(eq(newSummary), anyListOf(CensusSampleUnit.class), eq(SampleUnitState.INIT));
  }

  @Test(expected = Exception.class)
  public void missingColumns() throws Exception {
    SampleSummary newSummary = new SampleSummary();
    csvIngester.ingest(newSummary, getTestFile("census-survey-sample-missing-columns.csv"));
    verify(sampleService, times(0))
        .saveSample(eq(newSummary), anyListOf(CensusSampleUnit.class), eq(SampleUnitState.INIT));
    thrown.expect(CTPException.class);
  }

  @Test(expected = Exception.class)
  public void incorrectData() throws Exception {
    SampleSummary newSummary = new SampleSummary();
    csvIngester.ingest(newSummary, getTestFile("census-survey-sample-incorrect-data.csv"));
    verify(sampleService, times(0))
        .saveSample(eq(newSummary), anyListOf(CensusSampleUnit.class), eq(SampleUnitState.INIT));
    thrown.expect(CTPException.class);
  }
}
