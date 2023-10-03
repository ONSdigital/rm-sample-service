package uk.gov.ons.ctp.response.client;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import libs.common.rest.RestUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.representation.SampleReadinessRequestDTO;

/** HTTP RestClient implementation for calls to the Collection Exercise service. */
@Component
public class CollectionExerciseSvcClient {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseSvcClient.class);

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;

  public CollectionExerciseSvcClient(
      AppConfig appConfig,
      RestTemplate restTemplate,
      @Qualifier("collectionExerciseRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public void collectionExerciseSampleSummaryReadiness(UUID sampleSummaryId) {
    log.debug(
        "Notifying Collection Exercise of sample readiness {}",
        kv("sample_summary_id", sampleSummaryId));
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getCollectionExerciseSvc().getCollectionExerciseSampleSummaryReadiness(),
            null);
    SampleReadinessRequestDTO sampleReadinessNotificationDTO = new SampleReadinessRequestDTO();
    sampleReadinessNotificationDTO.setSampleSummaryId(sampleSummaryId);
    HttpEntity<SampleReadinessRequestDTO> httpEntity =
        restUtility.createHttpEntity(sampleReadinessNotificationDTO);
    System.out.println(uriComponents);
    System.out.println(httpEntity);
    restTemplate.exchange(uriComponents.toUri(), HttpMethod.POST, httpEntity, String.class);
  }
}
