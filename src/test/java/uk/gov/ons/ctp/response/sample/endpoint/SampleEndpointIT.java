package uk.gov.ons.ctp.response.sample.endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SampleEndpointIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void shouldUploadSampleFile() throws UnirestException, IOException {
        // Given
        final String sampleFile = "/csv/business-survey-sample.csv";

        // When
        HttpResponse<String> sampleSummaryResponse = Unirest.post("http://localhost:" + port + "/samples/bres/fileupload")
                .basicAuth("admin", "secret")
                .field("file", getClass().getResourceAsStream(sampleFile), ContentType.MULTIPART_FORM_DATA, "file")
                .asString();

        // Then
        assertThat(sampleSummaryResponse.getStatus()).isEqualTo(201);
        SampleSummary sampleSummary = mapper.readValue(sampleSummaryResponse.getBody(), new TypeReference<SampleSummary>() {});
        assertThat(sampleSummary.getState()).isEqualTo(SampleSummaryDTO.SampleState.INIT);
    }
}
