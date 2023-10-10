package uk.gov.ons.ctp.response.sample.client;

import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.UUID;
import libs.common.rest.RestUtility;
import libs.common.rest.RestUtilityConfig;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.client.CollectionExerciseSvcClient;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.CollectionExerciseSvc;

@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseSvcClientTest {
  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());
  @Mock private RestTemplate restTemplate;
  @Mock private AppConfig appConfig;

  @InjectMocks private CollectionExerciseSvcClient collectionExerciseSvcClient;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CollectionExerciseSvc collectionExerciseSvc = new CollectionExerciseSvc();
    when(appConfig.getCollectionExerciseSvc()).thenReturn(collectionExerciseSvc);
  }

  @Test
  public void notifyCollectionExerciseOfSampleReadiness() {
    // Given
    UUID sampleSummaryId = UUID.randomUUID();
    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
        .thenReturn(ResponseEntity.ok().build());

    // When
    collectionExerciseSvcClient.collectionExerciseSampleSummaryReadiness(sampleSummaryId);

    // Then
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
  }
}
