package uk.gov.ons.ctp.response.sample.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import libs.common.rest.RestUtility;
import libs.party.representation.PartyDTO;
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
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.message.EventPublisher;

@Service
public class PartySvcClientService {
  private static final Logger log = LoggerFactory.getLogger(PartySvcClientService.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Qualifier("partyRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private EventPublisher eventPublisher;

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public PartyDTO postParty(final PartyCreationRequestDTO partyCreationRequestDTO) {

    UriComponents uriComponents =
        restUtility.createUriComponents(appConfig.getPartySvc().getPostPartyPath(), null);

    HttpEntity<PartyCreationRequestDTO> httpEntity =
        restUtility.createHttpEntity(partyCreationRequestDTO);

    ResponseEntity<String> responseEntity =
        restTemplate.exchange(uriComponents.toUri(), HttpMethod.POST, httpEntity, String.class);

    PartyDTO party = null;
    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
      String responseBody = responseEntity.getBody();
      try {
        party = objectMapper.readValue(responseBody, PartyDTO.class);
      } catch (IOException e) {
        log.error("Unable to read party response", e);
      }
    }
    log.with("party", party).debug("Successfully retrieved party");
    eventPublisher.publishEvent("Sample PERSISTED");
    return party;
  }
}
