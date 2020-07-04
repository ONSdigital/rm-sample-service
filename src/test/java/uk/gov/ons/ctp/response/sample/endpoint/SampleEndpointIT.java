package uk.gov.ons.ctp.response.sample.endpoint;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import libs.common.UnirestInitialiser;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.response.lib.rabbit.Rabbitmq;
import uk.gov.ons.ctp.response.lib.rabbit.SimpleMessageListener;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

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

  @Test
  public void willReturn204OnExport() throws UnirestException, InterruptedException {
    String url =
        String.format(
            "http://localhost:%d/samples/export", port);

    HttpResponse<String> response =
        Unirest.post(url)
            .header("Content-Type", "application/json")
            .basicAuth("admin", "secret")
            .asString();

    assertThat(response.getStatus()).isEqualTo(204);
  }
}
