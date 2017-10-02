package uk.gov.ons.ctp.response.sample.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;

import java.io.IOException;

@Slf4j
@Service
public class PartySvcClientServiceImpl implements PartySvcClientService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Qualifier("partyRestUtility")
    @Autowired
    private RestUtility restUtility;

    @Autowired
    private ObjectMapper objectMapper;

    @Retryable(value = {RestClientException.class}, maxAttemptsExpression = "#{${retries.maxAttempts}}",
        backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
    @Override
    public PartyDTO postParty(final PartyCreationRequestDTO partyCreationRequestDTO) {

        UriComponents uriComponents = restUtility.createUriComponents(appConfig.getPartySvc().getPostPartyPath(),
            null);

        HttpEntity<PartyCreationRequestDTO> httpEntity = restUtility.createHttpEntity(partyCreationRequestDTO);

        ResponseEntity<String> responseEntity = restTemplate.exchange(uriComponents.toUri(), HttpMethod.POST,
            httpEntity, String.class);

        PartyDTO party = null;
        if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            try {
                party = objectMapper.readValue(responseBody, PartyDTO.class);
            } catch (IOException e) {
                String msg = String.format("cause = %s - message = %s", e.getCause(), e.getMessage());
                log.error(msg);
            }
        }
        log.debug("PARTY GOTTEN: {}", party);
        return party;
    }
}


