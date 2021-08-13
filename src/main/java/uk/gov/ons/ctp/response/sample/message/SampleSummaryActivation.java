package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** PubSub subscription responsible for receipt of sample units via PubSub. */
@Component
public class SampleSummaryActivation {
  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryActivation.class);
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SampleService sampleService;
  @Autowired AppConfig appConfig;

  /**
   * To process SampleUnit from PubSub This creates application ready event listener to provide an
   * active subscription for the new sample unit when published.
   *
   * @throws IOException
   */
  @EventListener(ApplicationReadyEvent.class)
  public void activateSampleSummary() throws IOException {
    log.debug("received SampleSummaryActivation message from PubSub");
    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Handle incoming message, then ack the received message.
          LOG.info("Receiving message ID from PubSub", kv(message.getMessageId()));
          LOG.with(message.getData().toString()).debug("Receiving data from PubSub ");
          try {
            SampleUnit sampleUnit =
                objectMapper.readValue(message.getData().toStringUtf8(), SampleUnit.class);
            sampleService.acceptSampleUnit(sampleUnit);
            consumer.ack();
          } catch (final IOException e) {
            log.with(e)
                .error(
                    "Something went wrong while processing message received from PubSub "
                        + "for sample unit notification");
            consumer.nack();
          }
        };
    Subscriber subscriber = getSampleUnitReceiverSubscriber(receiver);
    // Start the subscriber.
    subscriber.startAsync().awaitRunning();
    log.with(subscriber.getSubscriptionNameString())
        .info("Listening for sample unit notification messages on PubSub-subscription id");
  }

  /**
   * Provides PubSub subscriber for sample unit notification against message receiver
   *
   * @param receiver: com.google.cloud.pubsub.v1.MessageReceiver;
   * @return com.google.cloud.pubsub.v1.Subscriber;
   */
  private Subscriber getSampleUnitReceiverSubscriber(MessageReceiver receiver) throws IOException {
    if (StringUtil.isBlank(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      log.info("Returning Subscriber for sample unit notification");
      ExecutorProvider executorProvider =
          InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4).build();
      // `setParallelPullCount` determines how many StreamingPull streams the subscriber will open
      // to receive message. It defaults to 1. `setExecutorProvider` configures an executor for the
      // subscriber to process messages. Here, the subscriber is configured to open 2 streams for
      // receiving messages, each stream creates a new executor with 4 threads to help process the
      // message callbacks. In total 2x4=8 threads are used for message processing.
      return Subscriber.newBuilder(getSampleUnitSubscriptionName(), receiver)
          .setParallelPullCount(2)
          .setExecutorProvider(executorProvider)
          .build();
    } else {
      log.info("Returning emulator Subscriber");
      return new PubSubEmulator().getSampleUnitEmulatorSubscriber(receiver);
    }
  }

  /**
   * * Provides subscription name for the sample unit subscriber
   *
   * @return com.google.pubsub.v1.ProjectSubscriptionName
   */
  private ProjectSubscriptionName getSampleUnitSubscriptionName() {
    String project = appConfig.getGcp().getProject();
    String subscriptionId = appConfig.getGcp().getSampleUnitReceiverSubscription();
    log.with("Subscription id", subscriptionId)
        .with("project", project)
        .info("creating pubsub subscription name for sample unit notifications ");
    return ProjectSubscriptionName.of(project, subscriptionId);
  }
}
