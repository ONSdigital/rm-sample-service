package uk.gov.ons.ctp.response.sample.endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageListener;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleAttributesDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitRequest;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static uk.gov.ons.ctp.response.sample.UnirestInitialiser.initialiseUnirest;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class SampleEndpointIT {

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

    initialiseUnirest();
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
    SampleUnitRequest collexJobRequest =
        new SampleUnitRequest(
            UUID.randomUUID(), Collections.singletonList(sampleSummaryResponse.getBody().getId()));
    uploadFinishedMessageListener.take();

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
            new SampleUnit.SampleAttributes.Entry("Prem1", "14 ASHMEAD VIEW"),
            new SampleUnit.SampleAttributes.Entry("Postcode", "TS184QG"),
            new SampleUnit.SampleAttributes.Entry("PostTown", "STOCKTON-ON-TEES"),
            new SampleUnit.SampleAttributes.Entry("CountryCode", "E"));

    assertThat(sampleUnit.getId()).isNotNull();
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
            entry("Prem1", sampleAttribResponse.getBody().getAttributes().get("Prem1")),
            entry("Postcode", sampleAttribResponse.getBody().getAttributes().get("Postcode")),
            entry("PostTown", sampleAttribResponse.getBody().getAttributes().get("PostTown")),
            entry("CountryCode", sampleAttribResponse.getBody().getAttributes().get("CountryCode")),
            entry("Reference", sampleAttribResponse.getBody().getAttributes().get("Reference")));

    assertThat(sampleAttribResponse.getBody().getAttributes().get("Reference"))
        .isEqualTo("LMS0001");
  }

  private SampleSummaryDTO loadSocialSample(String sampleFile)
      throws UnirestException, IOException, InterruptedException {
    HttpResponse<SampleSummary> sampleSummaryResponse =
        Unirest.post("http://localhost:" + port + "/samples/SOCIAL/fileupload")
            .basicAuth("admin", "secret")
            .field(
                "file",
                getClass().getResourceAsStream(sampleFile),
                ContentType.MULTIPART_FORM_DATA,
                "file")
            .asObject(SampleSummary.class);

    log.debug("sampleSummaryResponse = " + sampleSummaryResponse.getBody());

    String message = uploadFinishedMessageListener.take();
    SampleSummaryDTO sampleSummary = mapper.readValue(message, SampleSummaryDTO.class);

    return sampleSummary;
  }
}
