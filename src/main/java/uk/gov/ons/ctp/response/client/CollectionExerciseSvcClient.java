package uk.gov.ons.ctp.response.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import libs.common.rest.RestUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.sample.config.AppConfig;

/** The service to retrieve a CollectionExercise */
@Service
public class CollectionExerciseSvcClient {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseSvcClient.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Qualifier("collectionExerciseRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Qualifier("customObjectMapper")
  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Informs collection exercise that the sample summary is valid
   *
   * @param valid true if its valid, otherwise false
   * @param collectionExerciseId the collection exercise id
   * @return true if successful
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public boolean validateSampleSummary(final boolean valid, final UUID collectionExerciseId) {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getCollectionExerciseSvc().getSampleValidatedPath(),
            null,
            valid,
            collectionExerciseId);
    HttpEntity<UriComponents> httpEntity = restUtility.createHttpEntity(uriComponents);

    ResponseEntity<String> response =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.POST, httpEntity, String.class);

    return response.getStatusCode().is2xxSuccessful();
  }

  /**
   * Informs collection exercise that the sample summary has been distributed
   *
   * @param valid true if its been distributed, otherwise false
   * @param collectionExerciseId the collection exercise id
   * @return true if successful
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public boolean distributedSampleSummary(final boolean valid, final UUID collectionExerciseId) {
    final UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getCollectionExerciseSvc().getSampleDistributedPath(),
            null,
            valid,
            collectionExerciseId);
    HttpEntity<UriComponents> httpEntity = restUtility.createHttpEntity(uriComponents);

    ResponseEntity<String> response =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.POST, httpEntity, String.class);

    return response.getStatusCode().is2xxSuccessful();
  }
}
