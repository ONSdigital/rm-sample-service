package uk.gov.ons.ctp.response.sample.endpoint;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Assert;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SampleEndpointIT {
  private static final Logger log = LoggerFactory.getLogger(SampleEndpointIT.class);

  @LocalServerPort private int port;

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

    Assert.assertEquals(204, response.getStatus());
  }
}
