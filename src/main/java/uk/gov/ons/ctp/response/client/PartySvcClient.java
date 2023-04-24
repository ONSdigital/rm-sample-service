package uk.gov.ons.ctp.response.client;

import static net.logstash.logback.argument.StructuredArguments.kv;

import libs.common.rest.RestUtility;
import libs.party.representation.PartyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.representation.SampleLinkCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleLinkDTO;

/** HTTP RestClient implementation for calls to the Party service */
@Component
public class PartySvcClient {
  private static final Logger log = LoggerFactory.getLogger(PartySvcClient.class);

  private AppConfig appConfig;
  private RestTemplate restTemplate;
  private RestUtility restUtility;

  public PartySvcClient(
      AppConfig appConfig,
      RestTemplate restTemplate,
      @Qualifier("partyRestUtility") RestUtility restUtility) {
    this.appConfig = appConfig;
    this.restTemplate = restTemplate;
    this.restUtility = restUtility;
  }

  /**
   * Request party from the Party Service
   *
   * @param sampleUnitRef the sample unit ref for which to request party
   * @return the party object
   */
  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public PartyDTO requestParty(String sampleUnitRef) {
    log.debug("Retrieving party {}", kv("sample_unit_ref", sampleUnitRef));
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getPartySvc().getRequestPartyPath(), null, sampleUnitRef);
    HttpEntity<PartyDTO> httpEntity = restUtility.createHttpEntityWithAuthHeader();
    ResponseEntity<PartyDTO> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, httpEntity, PartyDTO.class);
    return responseEntity.getBody();
  }

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public void linkSampleSummaryId(String sampleSummaryId, String collectionExerciseId) {
    log.debug(
        "Linking sample summary to collection exercise {} {}",
        kv("sample_summary_id", sampleSummaryId),
        kv("collection_exercise_id", collectionExerciseId));
    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getPartySvc().getSampleLinkPath(), null, sampleSummaryId);
    SampleLinkCreationRequestDTO sampleLinkCreationRequestDTO = new SampleLinkCreationRequestDTO();
    sampleLinkCreationRequestDTO.setCollectionExerciseId(collectionExerciseId);
    HttpEntity<SampleLinkCreationRequestDTO> httpEntity =
        restUtility.createHttpEntity(sampleLinkCreationRequestDTO);
    restTemplate.exchange(uriComponents.toUri(), HttpMethod.PUT, httpEntity, SampleLinkDTO.class);
  }
}
