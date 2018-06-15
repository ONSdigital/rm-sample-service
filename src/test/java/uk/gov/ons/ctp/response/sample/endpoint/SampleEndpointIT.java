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
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageListener;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ons.ctp.response.sample.UnirestInitialiser.initialiseUnirest;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class SampleEndpointIT {

    @Autowired
    private AppConfig appConfig;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper mapper;
    private SimpleMessageListener sml;
    private BlockingQueue<String> uploadFinishedMessageListener;
    private BlockingQueue<String> sampleDeliveryMessageListener;

    @Before
    public void setUp() {
        Rabbitmq config = this.appConfig.getRabbitmq();
        sml = new SimpleMessageListener(config.getHost(), config.getPort(), config.getUsername(),
                config.getPassword());

        uploadFinishedMessageListener = sml.listen(SimpleMessageListener.ExchangeType.Direct,
                "sample-outbound-exchange", "Sample.SampleUploadFinished.binding");

        sampleDeliveryMessageListener = sml.listen(SimpleMessageListener.ExchangeType.Direct,
                "sample-outbound-exchange", "Sample.SampleDelivery.binding");

        initialiseUnirest();
    }

    @After
    public void tearDown() {
        sml.close();
    }


    @Test
    public void shouldUploadBusinessSampleFile() throws UnirestException, IOException, InterruptedException {
        log.info("STARTING - shouldUploadBusinessSampleFile");
        // Given
        final String sampleFile = "/csv/business-survey-sample.csv";

        // When
        HttpResponse<String> sampleSummaryResponse =
                Unirest.post("http://localhost:" + port + "/samples/B/fileupload")
                .basicAuth("admin", "secret")
                .field("file", getClass().getResourceAsStream(sampleFile), ContentType.MULTIPART_FORM_DATA, "file.csv")
                .asString();

        // Then
        assertThat(sampleSummaryResponse.getStatus()).isEqualTo(201);
        SampleSummary sampleSummary = mapper.readValue(sampleSummaryResponse.getBody(), new TypeReference<SampleSummary>() {});
        assertThat(sampleSummary.getState()).isEqualTo(SampleState.INIT);

        String message = uploadFinishedMessageListener.take();
        SampleSummaryDTO active = mapper.readValue(message, SampleSummaryDTO.class);

        assertThat(active.getExpectedCollectionInstruments()).isEqualTo(1);
        assertThat(active.getTotalSampleUnits()).isEqualTo(1);
        log.info("ENDING - shouldUploadBusinessSampleFile");
    }

    @Test
    public void shouldUploadSocialSampleFile() throws UnirestException, IOException {
        log.info("STARTING - shouldUploadSocialSampleFile");
        // Given
        final String sampleFile = "/csv/social-survey-sample.csv";

        // When
        HttpResponse<String> sampleSummaryResponse =
                Unirest.post("http://localhost:" + port + "/samples/SOCIAL/fileupload")
                        .basicAuth("admin", "secret")
                        .field("file", getClass().getResourceAsStream(sampleFile), ContentType.MULTIPART_FORM_DATA, "file.csv")
                        .asString();

        // Then
        assertThat(sampleSummaryResponse.getStatus()).isEqualTo(201);
        SampleSummary sampleSummary = mapper.readValue(sampleSummaryResponse.getBody(), new TypeReference<SampleSummary>() {});
        assertThat(sampleSummary.getState()).isEqualTo(SampleSummaryDTO.SampleState.INIT);
        log.info("FINISHING - shouldUploadSocialSampleFile");

    }

    @Test
    public void shouldPutSampleAttributesOnQueueWhenSampleUnitsRequested() throws UnirestException, InterruptedException, JAXBException {
        log.info("STARTING - shouldPutSampleAttributesOnQueueWhenSampleUnitsRequested");
        // Given
        final String sampleFile = "/csv/social-survey-sample.csv";
        HttpResponse<SampleSummary> sampleSummaryResponse =
                Unirest.post("http://localhost:" + port + "/samples/SOCIAL/fileupload")
                        .basicAuth("admin", "secret")
                        .field("file", getClass().getResourceAsStream(sampleFile), ContentType.MULTIPART_FORM_DATA, "file.csv")
                        .asObject(SampleSummary.class);
        CollectionExerciseJobCreationRequestDTO collexJobRequest = new CollectionExerciseJobCreationRequestDTO(
                UUID.randomUUID(), "TEST", new Date(), Collections.singletonList(sampleSummaryResponse.getBody().getId()));

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

        JAXBContext jaxbContext = JAXBContext.newInstance(SampleUnit.class);
        SampleUnit sampleUnit = (SampleUnit) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(message.getBytes()));
        assertThat(sampleUnit.getSampleAttributes().getEntries()).contains(
                new SampleUnit.SampleAttributes.Entry("Prem1", "14 ASHMEAD VIEW"),
                new SampleUnit.SampleAttributes.Entry("Postcode", "TS184QG"),
                new SampleUnit.SampleAttributes.Entry("PostTown", "STOCKTON-ON-TEES"),
                new SampleUnit.SampleAttributes.Entry("CountryCode", "E"));
        log.info("FINISHING - shouldPutSampleAttributesOnQueueWhenSampleUnitsRequested");
    }


}
