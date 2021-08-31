package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.SampleSvcApplication;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** SampleUnitPublisher publishes message to PubSub */
@Service
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SampleSvcApplication.PubSubOutboundSampleUnitGateway samplePublisher;

  @Autowired
  private SampleSvcApplication.PubSubOutboundCaseNotificationGateway caseNotificationPublisher;

  /**
   * send sample to collection exercise via PubSub
   *
   * @param sampleUnit to be sent
   */
  public void send(SampleUnit sampleUnit) {
    log.debug("send to queue sampleDelivery", kv("sample_unit", sampleUnit));
    try {
      String message = objectMapper.writeValueAsString(sampleUnit);
      log.info("Publishing message to PubSub", kv("sample_unit", sampleUnit.getSampleUnitRef()));
      samplePublisher.sendToPubSub(message);
      log.info(
          "Sample Unit published to PubSub successfully",
          kv("sample_unit", sampleUnit.getSampleUnitRef()));
    } catch (JsonProcessingException e) {
      log.error("Error while sample unit can not be parsed.", kv("sampleUnit", sampleUnit));
      throw new RuntimeException(e);
    }
  }

  /**
   * send sample to case via PubSub
   *
   * @param sampleUnit to be sent
   */
  public void sendSampleUnitToCase(SampleUnitParentDTO sampleUnit) {
    try {
      log.info("Publishing message to PubSub", kv("sampleUnitRef", sampleUnit.getSampleUnitRef()));
      String message = objectMapper.writeValueAsString(sampleUnit);
      caseNotificationPublisher.sendToPubSub(message);
      log.info(
          "Sample unit to case publish sent successfully",
          kv("sampleUnitRef", sampleUnit.getSampleUnitRef()));
    } catch (JsonProcessingException e) {
      log.error("Error converting sample unit to JSON", kv("sampleUnit", sampleUnit));
      throw new RuntimeException(e);
    }
  }
}
