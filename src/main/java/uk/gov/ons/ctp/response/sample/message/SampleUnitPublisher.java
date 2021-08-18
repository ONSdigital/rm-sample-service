package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
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

  @Autowired private PubSub pubSub;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SampleSvcApplication.PubSubOutboundSampleUnitGateway samplePublisher;

  /**
   * send sample to collection exercise via PubSub
   *
   * @param sampleUnit to be sent
   */
  public void send(SampleUnit sampleUnit) {
    log.debug("send to queue sampleDelivery", kv("sample_unit", sampleUnit));
    try {
      String message = objectMapper.writeValueAsString(sampleUnit);
      log.info("Publishing message to PubSub");
      samplePublisher.sendToPubSub(message);
      log.info("Sample Unit published to PubSub successfully");
    } catch (JsonProcessingException e) {
      log.error("Error while sample unit can not be parsed.", kv("sampleUnit", sampleUnit));
      throw new RuntimeException(e);
    }
  }

  /**
   * Sends a sample unit to case via PubSub
   *
   * @param sampleUnit A sample unit to be sent
   */
  public void sendSampleUnitToCase(SampleUnitParentDTO sampleUnit) {
    log.debug(
        "Entering sendSampleUnit",
        kv("sample_unit_type", sampleUnit.getSampleUnitType()),
        kv("sample_unit_ref", sampleUnit.getSampleUnitRef()));
    try {
      String message = objectMapper.writeValueAsString(sampleUnit);
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      Publisher publisher = pubSub.caseNotificationPublisher();
      try {
        log.info("Publishing message to PubSub");
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        ApiFutures.addCallback(
            messageIdFuture,
            new ApiFutureCallback<>() {
              @Override
              public void onFailure(Throwable throwable) {
                if (throwable instanceof ApiException) {
                  ApiException apiException = ((ApiException) throwable);
                  log.error(
                      "SampleUnit publish sent failure to PubSub.",
                      kv("error", apiException.getStatusCode().getCode()));
                }
                log.error("Error Publishing PubSub message", kv("message", message));
              }

              @Override
              public void onSuccess(String messageId) {
                // Once published, returns server-assigned message ids (unique within the topic)
                log.info("SampleUnit publish sent successfully", kv("messageId", messageId));
              }
            },
            MoreExecutors.directExecutor());
      } finally {
        publisher.shutdown();
        pubSub.shutdown();
      }
    } catch (JsonProcessingException e) {
      log.error("Error while sampleUnit can not be parsed.", kv("sampleUnit", sampleUnit));
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("PubSub Error while processing sample unit distribution", e);
      throw new RuntimeException(e);
    }
  }
}
