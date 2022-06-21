package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.util.UUID;

import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.config.AppConfig;

@Component
public class SampleDeadLetterPubSubSubscriber {
  private static final Logger log = LoggerFactory.getLogger(SampleDeadLetterPubSubSubscriber.class);
  @Autowired AppConfig appConfig;
  @Autowired private SampleDeadLetterReceiver sampleDeadLetterReceiver;
  @Autowired private PubSub pubSub;

  /**
   * Sets up a connection to the sample dead letter subscription. A sample is added to the dead
   * letter queue after 10 failed processing attempts
   */
  @EventListener(ApplicationReadyEvent.class)
  public void sampleDeadLetterSubscription() throws IOException {
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(
            appConfig.getGcp().getProject(),
            appConfig.getGcp().getSampleSummaryDeadLetterSubscription());
    MessageReceiver receiver = createMessageReceiver();
    Subscriber subscriber = pubSub.getSampleDeadLetterSubscriber(receiver);
    subscriber.startAsync().awaitRunning();
    log.info(
        "Listening for messages on subscription", kv("subscription", subscriptionName.toString()));
  }

  public MessageReceiver createMessageReceiver() {
    return (PubsubMessage message, AckReplyConsumer consumer) -> {
      String payload = message.getData().toStringUtf8();
      UUID sampleSummaryId = UUID.fromString(message.getAttributesOrDefault("sample_summary_id", "default"));
      log.info("Received a dead lettered sample", kv("payload", payload));
      try {
        sampleDeadLetterReceiver.process(sampleSummaryId);
      } catch (CTPException e) {
        log.error("Error processing sample", e);
        consumer.nack();
      } catch (Exception e) {
        log.error("Unexpected error processing sample", e);
        consumer.nack();
      }
      consumer.ack();
    };
  }
}
