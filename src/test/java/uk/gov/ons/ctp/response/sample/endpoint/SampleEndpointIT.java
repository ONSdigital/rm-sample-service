package uk.gov.ons.ctp.response.sample.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.common.UnirestInitialiser;
import uk.gov.ons.ctp.response.libs.sample.definition.SampleUnit;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleAttributesDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.tools.rabbit.Rabbitmq;
import uk.gov.ons.tools.rabbit.SimpleMessageListener;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SampleEndpointIT {
  private static final Logger log = LoggerFactory.getLogger(SampleEndpointIT.class);

  @Autowired private AppConfig appConfig;

  @LocalServerPort private int port;

  @Autowired private ObjectMapper mapper;
  private SimpleMessageListener sml;

  private BlockingQueue<String> uploadFinishedMessageListener;
  private BlockingQueue<String> sampleDeliveryMessageListener;

  @Before
  public void setUp() {
    Rabbitmq config = this.appConfig.getRabbitmq();
    sml =
        new SimpleMessageListener(
            config.getHost(), config.getPort(), config.getUsername(), config.getPassword());

    uploadFinishedMessageListener =
        sml.listen(
            SimpleMessageListener.ExchangeType.Direct,
            "sample-outbound-exchange",
            "Sample.SampleUploadFinished.binding");

    sampleDeliveryMessageListener =
        sml.listen(
            SimpleMessageListener.ExchangeType.Direct,
            "sample-outbound-exchange",
            "Sample.SampleDelivery.binding");

    UnirestInitialiser.initialise(mapper);
  }

  @After
  public void tearDown() {
    sml.close();
  }

  @Test
  public void shouldUploadBusinessSampleFile()
      throws UnirestException, IOException, InterruptedException {
    // Given
    final String sampleFile = "/csv/business-survey-sample.csv";

    // When
    HttpResponse<String> sampleSummaryResponse =
        Unirest.post("http://localhost:" + port + "/samples/B/fileupload")
            .basicAuth("admin", "secret")
            .field(
                "file",
                getClass().getResourceAsStream(sampleFile),
                ContentType.MULTIPART_FORM_DATA,
                "file")
            .asString();

    // Then
    assertThat(sampleSummaryResponse.getStatus()).isEqualTo(201);
    SampleSummary sampleSummary =
        mapper.readValue(sampleSummaryResponse.getBody(), new TypeReference<SampleSummary>() {});
    assertThat(sampleSummary.getState()).isEqualTo(SampleState.INIT);

    String message = uploadFinishedMessageListener.take();
    SampleSummaryDTO active = mapper.readValue(message, SampleSummaryDTO.class);

    assertThat(active.getExpectedCollectionInstruments()).isEqualTo(1);
    assertThat(active.getTotalSampleUnits()).isEqualTo(1);
  }

  @Test
  public void shouldUploadSocialSampleFile() throws UnirestException, IOException {
    // Given
    final String sampleFile = "/csv/social-survey-sample.csv";

    // When
    HttpResponse<String> sampleSummaryResponse =
        Unirest.post("http://localhost:" + port + "/samples/SOCIAL/fileupload")
            .basicAuth("admin", "secret")
            .field(
                "file",
                getClass().getResourceAsStream(sampleFile),
                ContentType.MULTIPART_FORM_DATA,
                "file")
            .asString();

    // Then
    assertThat(sampleSummaryResponse.getStatus()).isEqualTo(201);
    SampleSummary sampleSummary =
        mapper.readValue(sampleSummaryResponse.getBody(), new TypeReference<SampleSummary>() {});
    assertThat(sampleSummary.getState()).isEqualTo(SampleSummaryDTO.SampleState.INIT);
  }

  @Test
  public void shouldPutSampleIdAndAttributesOnQueueWhenSampleUnitsRequested()
      throws UnirestException, InterruptedException, JAXBException {
    // Given
    final String sampleFile = "/csv/social-survey-sample.csv";
    HttpResponse<SampleSummary> sampleSummaryResponse =
        Unirest.post("http://localhost:" + port + "/samples/SOCIAL/fileupload")
            .basicAuth("admin", "secret")
            .field(
                "file",
                getClass().getResourceAsStream(sampleFile),
                ContentType.MULTIPART_FORM_DATA,
                "file")
            .asObject(SampleSummary.class);
    CollectionExerciseJobCreationRequestDTO collexJobRequest =
        new CollectionExerciseJobCreationRequestDTO(
            UUID.randomUUID(),
            "TEST",
            new Date(),
            Collections.singletonList(sampleSummaryResponse.getBody().getId()));

    // When
    HttpResponse<String> sampleUnitResponse =
        Unirest.post("http://localhost:" + port + "/samples/sampleunitrequests")
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .body(collexJobRequest)
            .asString();

    // Then
    assertThat(sampleUnitResponse.getStatus()).isEqualTo(201);
    String message = sampleDeliveryMessageListener.take();

    log.debug("message = " + message);

    JAXBContext jaxbContext = JAXBContext.newInstance(SampleUnit.class);
    SampleUnit sampleUnit =
        (SampleUnit)
            jaxbContext
                .createUnmarshaller()
                .unmarshal(new ByteArrayInputStream(message.getBytes()));

    assertThat(sampleUnit.getSampleAttributes().getEntries())
        .contains(
            new SampleUnit.SampleAttributes.Entry("ADDRESS_LINE2", "11 HILL VIEW"),
            new SampleUnit.SampleAttributes.Entry("POSTCODE", "AA11AA"),
            new SampleUnit.SampleAttributes.Entry("TOWN_NAME", "STOCKTON-ON-TEES"),
            new SampleUnit.SampleAttributes.Entry("COUNTRY", "E"),
            new SampleUnit.SampleAttributes.Entry("TLA", "LMS"),
            new SampleUnit.SampleAttributes.Entry("LOCALITY", "FAKEVILL"),
            new SampleUnit.SampleAttributes.Entry("UPRN", "123456"),
            new SampleUnit.SampleAttributes.Entry("REFERENCE", "0001"));

    assertThat(sampleUnit.getId()).isNotNull();
    assertThat(sampleUnit.getSampleUnitRef()).isEqualTo("LMS0001");
  }

  @Test
  public void ensureAttributesReturnedWhenGettingSample()
      throws UnirestException, IOException, InterruptedException {

    SampleSummaryDTO sampleSummary = loadSocialSample("/csv/social-survey-sample-ref.csv");

    HttpResponse<SampleUnitDTO[]> sampleUnits =
        Unirest.get(
                String.format(
                    "http://localhost:%d/samples/%s/sampleunits", port, sampleSummary.getId()))
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .asObject(SampleUnitDTO[].class);

    String id = sampleUnits.getBody()[0].getId();

    assertThat(id).isNotBlank();

    HttpResponse<SampleAttributesDTO> sampleAttribResponse =
        Unirest.get(String.format("http://localhost:%d/samples/%s/attributes", port, id))
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .asObject(SampleAttributesDTO.class);

    log.debug("res = {}", sampleAttribResponse.getBody());

    HttpResponse<SampleUnitDTO> sampleUnitResponse =
        Unirest.get(String.format("http://localhost:%d/samples/%s", port, id))
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .asObject(SampleUnitDTO.class);

    assertThat(sampleUnitResponse.getBody().getId()).isEqualTo(id);
    assertThat(sampleAttribResponse.getBody().getId().toString()).isEqualTo(id);

    assertThat(sampleUnitResponse.getBody().getSampleAttributes().getAttributes().entrySet())
        .contains(
            entry(
                "ADDRESS_LINE1",
                sampleAttribResponse.getBody().getAttributes().get("ADDRESS_LINE1")),
            entry(
                "ADDRESS_LINE2",
                sampleAttribResponse.getBody().getAttributes().get("ADDRESS_LINE2")),
            entry("POSTCODE", sampleAttribResponse.getBody().getAttributes().get("POSTCODE")),
            entry("TOWN_NAME", sampleAttribResponse.getBody().getAttributes().get("TOWN_NAME")),
            entry("COUNTRY", sampleAttribResponse.getBody().getAttributes().get("COUNTRY")),
            entry("LOCALITY", sampleAttribResponse.getBody().getAttributes().get("LOCALITY")),
            entry("TLA", sampleAttribResponse.getBody().getAttributes().get("TLA")),
            entry("UPRN", sampleAttribResponse.getBody().getAttributes().get("UPRN")),
            entry("REFERENCE", sampleAttribResponse.getBody().getAttributes().get("REFERENCE")));

    assertThat(sampleAttribResponse.getBody().getAttributes().get("REFERENCE")).isEqualTo("0002");
  }

  @Test
  public void getSampleUnitSizeHappyPath() throws UnirestException, InterruptedException {
    final String sampleFile = "/csv/business-survey-sample-multiple.csv";
    HttpResponse<SampleSummary> sampleSummary =
        Unirest.post("http://localhost:" + port + "/samples/B/fileupload")
            .basicAuth("admin", "secret")
            .field(
                "file",
                getClass().getResourceAsStream(sampleFile),
                ContentType.MULTIPART_FORM_DATA,
                "file")
            .asObject(SampleSummary.class);

    String sampleSummaryId = sampleSummary.getBody().getId().toString();

    uploadFinishedMessageListener.take();

    String url =
        String.format(
            "http://localhost:%d/samples/count?sampleSummaryId=%s", port, sampleSummaryId);

    HttpResponse<SampleUnitsRequestDTO> response =
        Unirest.get(url)
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .asObject(SampleUnitsRequestDTO.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getBody().getSampleUnitsTotal()).isEqualTo(4);
  }

  private SampleSummaryDTO loadSocialSample(String sampleFile)
      throws UnirestException, IOException, InterruptedException {
    HttpResponse<SampleSummary> sampleSummaryResponse =
        Unirest.post(String.format("http://localhost:%d/samples/SOCIAL/fileupload", port))
            .basicAuth("admin", "secret")
            .field(
                "file",
                getClass().getResourceAsStream(sampleFile),
                ContentType.MULTIPART_FORM_DATA,
                "file")
            .asObject(SampleSummary.class);

    log.debug("sampleSummaryResponse = " + sampleSummaryResponse.getBody());

    String message = uploadFinishedMessageListener.take();

    return mapper.readValue(message, SampleSummaryDTO.class);
  }
}
