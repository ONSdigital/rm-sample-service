package uk.gov.ons.ctp.response.sample.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import libs.common.rest.RestUtility;
import libs.common.rest.RestUtilityConfig;
import libs.party.representation.PartyDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.PartySvc;

@RunWith(MockitoJUnitRunner.class)
public class PartySvcClientTest {

  private static final String SAMPLE_UNIT_REF = "testRef";
  private static final String SAMPLE_SUMMARY_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";

  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @InjectMocks private PartySvcClient partySvcClient;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    PartySvc partySvc = new PartySvc();
    when(appConfig.getPartySvc()).thenReturn(partySvc);
  }

  @Test
  public void getParty_200Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

    // When
    partySvcClient.requestParty(SAMPLE_UNIT_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class));
  }

  @Test(expected = HttpClientErrorException.class)
  public void getParty_500Response() {
    // Given
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    // When
    partySvcClient.requestParty(SAMPLE_UNIT_REF);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PartyDTO.class));
  }
}
