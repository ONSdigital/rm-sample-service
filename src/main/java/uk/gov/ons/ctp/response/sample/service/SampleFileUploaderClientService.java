package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import libs.common.rest.RestUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import uk.gov.ons.ctp.response.sample.config.AppConfig;

@Service
public class SampleFileUploaderClientService {
  private static final Logger log = LoggerFactory.getLogger(SampleFileUploaderClientService.class);

  @Autowired private AppConfig appConfig;

  @Autowired private RestTemplate restTemplate;

  @Qualifier("partyRestUtility")
  @Autowired
  private RestUtility restUtility;

  @Retryable(
      value = {RestClientException.class},
      maxAttemptsExpression = "#{${retries.maxAttempts}}",
      backoff = @Backoff(delayExpression = "#{${retries.backoff}}"))
  public boolean sendSampleFile(final String file, final String sampleSummaryId) {

    UriComponents uriComponents =
        restUtility.createUriComponents(
            appConfig.getSampleFileUploader().getSampleUploadPath(), null, sampleSummaryId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", file);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    String url = uriComponents.toUriString();

    log.info("About to send to " + url);

    ResponseEntity<String> response =
        restTemplate.postForEntity(uriComponents.toUri(), requestEntity, String.class);
    if (response != null && response.getStatusCode().is2xxSuccessful()) {
      String responseBody = response.getBody();
      log.debug("Response received", kv("responseBody", responseBody));
      log.info("Successfully sent sample file", kv("sampleSummaryId", sampleSummaryId));
      return true;
    } else {
      return false;
    }
  }
}
