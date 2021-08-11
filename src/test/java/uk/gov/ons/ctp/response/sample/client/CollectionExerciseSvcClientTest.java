package uk.gov.ons.ctp.response.sample.client;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.client.CollectionExerciseSvcClient;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.CollectionExerciseSvc;

@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseSvcClientTest {

  @Mock private RestTemplate restTemplate;
  @Mock private AppConfig appConfig;
  @Spy private RestUtility restUtility = new RestUtility(RestUtilityConfig.builder().build());

  @InjectMocks private CollectionExerciseSvcClient collectionExerciseSvcClient;

  @Before
  public void setUp() {
    CollectionExerciseSvc collectionExerciseSvc = new CollectionExerciseSvc();
    collectionExerciseSvc.setSampleDistributedPath(
        "/collectionexerciseexecution/distributed/{distributed}/collectionexercise/{collectionExerciseId}");
    collectionExerciseSvc.setSampleValidatedPath(
        "/collectionexerciseexecution/validated/{valid}/collectionexercise/{collectionExerciseId}");
    when(appConfig.getCollectionExerciseSvc()).thenReturn(collectionExerciseSvc);
  }

  @Test
  public void testValidateSampleSummary() {
    UUID collectionExerciseId = UUID.randomUUID();
    boolean valid = true;

    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    assertTrue(collectionExerciseSvcClient.validateSampleSummary(valid, collectionExerciseId));
  }

  @Test
  public void testValidateSampleSummaryFailed() {
    UUID collectionExerciseId = UUID.randomUUID();
    boolean valid = true;

    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    assertFalse(collectionExerciseSvcClient.validateSampleSummary(valid, collectionExerciseId));
  }

  @Test
  public void testDistributedSampleSummary() {
    UUID collectionExerciseId = UUID.randomUUID();
    boolean valid = true;

    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    assertTrue(collectionExerciseSvcClient.distributedSampleSummary(valid, collectionExerciseId));
  }

  @Test
  public void testDistributedSampleSummaryFailed() {
    UUID collectionExerciseId = UUID.randomUUID();
    boolean valid = true;

    when(restTemplate.exchange(
            any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    assertFalse(collectionExerciseSvcClient.distributedSampleSummary(valid, collectionExerciseId));
  }
}
