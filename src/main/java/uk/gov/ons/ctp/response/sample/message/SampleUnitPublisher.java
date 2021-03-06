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
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** SampleUnitPublisher publishes message to PubSub */
@Service
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);

  @Autowired private PubSub pubSub;

  @Autowired private ObjectMapper objectMapper;

  /**
   * send sample to collection exercise via PubSub
   *
   * @param sampleUnit to be sent
   */
  public void send(SampleUnit sampleUnit) {
    log.debug("send to queue sampleDelivery", kv("sample_unit", sampleUnit));
    try {
      String message = objectMapper.writeValueAsString(sampleUnit);
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      Publisher publisher = pubSub.sampleUnitPublisher();
      try {
        log.info("Publishing message to PubSub", kv("publisher", publisher));
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        ApiFutures.addCallback(
            messageIdFuture,
            new ApiFutureCallback<>() {
              @Override
              public void onFailure(Throwable throwable) {
                if (throwable instanceof ApiException) {
                  ApiException apiException = ((ApiException) throwable);
                  log.error(
                      "Sample Unit sent failure",
                      kv("error", apiException.getStatusCode().getCode()));
                }
                log.error("Error Publishing PubSub message", kv("message", message));
              }

              @Override
              public void onSuccess(String messageId) {
                // Once published, returns server-assigned message ids (unique within the topic)
                log.info(
                    "Sample Unit published to PubSub successfully", kv("messageId", messageId));
              }
            },
            MoreExecutors.directExecutor());
      } finally {
        publisher.shutdown();
        pubSub.shutdown();
      }
    } catch (JsonProcessingException e) {
      log.error("Error while sample unit can not be parsed.", kv("sampleUnit", sampleUnit));
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("PubSub Error while processing sample unit publish", e);
      throw new RuntimeException(e);
    }
  }
}
