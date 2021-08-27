package uk.gov.ons.ctp.response.sample.endpoint;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application-test.yml")
public class SampleEndpointIT {
  private static final Logger log = LoggerFactory.getLogger(SampleEndpointIT.class);

  @ClassRule
  public static final EnvironmentVariables environmentVariables =
      new EnvironmentVariables().set("PUBSUB_EMULATOR_HOST", "127.0.0.1:18681");

  @LocalServerPort private int port;
  @MockBean private CollectionExerciseJobRepository collectionExerciseJobRepository;
  @MockBean private SampleSummaryRepository sampleSummaryRepository;
  @MockBean private SampleUnitRepository sampleUnitRepository;

  @Test
  public void willReturn204OnExport() throws UnirestException, InterruptedException, IOException {
    UUID collexId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID sampleUnitId = UUID.randomUUID();
    List<CollectionExerciseJob> collexJobs = new ArrayList<>();
    CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
    collectionExerciseJob.setCollectionExerciseId(collexId);
    collectionExerciseJob.setCollectionExerciseJobPK(1);
    collectionExerciseJob.setJobComplete(false);
    collectionExerciseJob.setSampleSummaryId(sampleSummaryId);
    collectionExerciseJob.setSurveyRef("12345");
    collexJobs.add(collectionExerciseJob);
    BDDMockito.given(collectionExerciseJobRepository.findByJobCompleteIsFalse())
        .willReturn(collexJobs);
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setDescription("test");
    sampleSummary.setExpectedCollectionInstruments(1);
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);
    BDDMockito.given(sampleSummaryRepository.findById(sampleSummaryId))
        .willReturn(Optional.of(sampleSummary));
    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setFormType("B");
    sampleUnit.setId(sampleUnitId);
    SampleAttributes sampleAttributes = new SampleAttributes();
    Map<String, String> attr = new HashMap<>();
    attr.put("test", "test");
    sampleAttributes.setAttributes(attr);
    sampleAttributes.setSampleUnitFK(UUID.randomUUID());
    sampleUnit.setSampleAttributes(sampleAttributes);
    sampleUnit.setSampleSummaryFK(1);
    sampleUnit.setSampleUnitPK(1);
    sampleUnit.setSampleUnitRef("test");
    sampleUnit.setSampleUnitType("test");
    sampleUnit.setState(SampleUnitDTO.SampleUnitState.PERSISTED);
    BDDMockito.given(
            sampleUnitRepository.findBySampleSummaryFKAndState(
                1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .willReturn(Stream.of(sampleUnit));
    BDDMockito.given(sampleUnitRepository.findById(sampleUnitId))
        .willReturn(Optional.of(sampleUnit));
    TestPubSubMessage message = new TestPubSubMessage();
    String url = String.format("http://localhost:%d/samples/export", port);

    HttpResponse<String> response =
        Unirest.post(url)
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .asString();
    Assert.assertEquals(204, response.getStatus());

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit sampleUnitDTO =
        message.getPubSubSampleUnit();
    Assert.assertEquals(sampleUnitDTO.getCollectionExerciseId(), collexId.toString());
    Assert.assertEquals(sampleUnitDTO.getSampleUnitRef(), "test");
    Assert.assertEquals(sampleUnitDTO.getSampleUnitType(), "test");
  }
}
