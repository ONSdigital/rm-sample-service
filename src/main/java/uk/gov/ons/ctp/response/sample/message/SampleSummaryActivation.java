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
import java.io.IOException;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;
import uk.gov.ons.ctp.response.sample.service.*;
import uk.gov.ons.ctp.response.sample.utility.PubSubEmulator;

/** PubSub subscription responsible for sample summary activation via PubSub. */
@Component
public class SampleSummaryActivation {
  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryActivation.class);
  @Autowired private ObjectMapper objectMapper;

  @Autowired AppConfig appConfig;

  @Autowired SampleSummaryActivationStatusPublisher sampleSummaryActivationStatusPublisher;

  @Autowired SampleSummaryActivationService sampleSummaryActivationService;

  @Autowired SampleSummaryEnrichmentService sampleSummaryEnrichmentService;

  @Autowired SampleSummaryDistributionService sampleSummaryDistributionService;

  /**
   * To process Sample summary activation from PubSub.
   *
   * @throws IOException
   */
  @EventListener(ApplicationReadyEvent.class)
  public void activateSampleSummary() throws IOException {
    LOG.debug("Creating SampleSummaryActivation subscription from application ready event");
    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Handle incoming message, then ack the received message.
          LOG.info("Receiving message ID from PubSub", kv("messageId", message.getMessageId()));
          LOG.debug("Receiving data from PubSub ", kv("data", message.getData().toString()));
          try {
            SampleSummaryActivationDTO sampleSummaryActivation =
                objectMapper.readValue(
                    message.getData().toStringUtf8(), SampleSummaryActivationDTO.class);

            // We ack here now that the message has be deserialized correctly.  If something goes
            // wrong then
            // we'll inform collection exercise via a separate pubsub message.
            consumer.ack();
            sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);

          } catch (final IOException e) {
            LOG.error(
                "Something went wrong while processing message received from PubSub "
                    + "for sample unit notification",
                e);
            // Only nack on this specific if we can't deserialize it
            consumer.nack();
          } catch (final SampleSummaryActivationException e) {
            LOG.error("Something went wrong during the sample summary activation", e);
            // Intentionally not nacking as we've already told collection exercise of the
            // failure.
          }
        };
    Subscriber subscriber = getSampleSummaryActivationSubscriber(receiver);
    // Start the subscriber.
    subscriber.startAsync().awaitRunning();
    LOG.info(
        "Listening for sample unit notification messages on PubSub-subscription id",
        kv("subscriptionId", subscriber.getSubscriptionNameString()));
  }

  /**
   * Provides PubSub subscriber for sample unit notification against message receiver
   *
   * @param receiver: com.google.cloud.pubsub.v1.MessageReceiver;
   * @return com.google.cloud.pubsub.v1.Subscriber;
   */
  private Subscriber getSampleSummaryActivationSubscriber(MessageReceiver receiver)
      throws IOException {
    if (StringUtil.isBlank(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      LOG.info("Returning subscriber for sample summary activation");
      ExecutorProvider executorProvider =
          InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4).build();
      // `setParallelPullCount` determines how many StreamingPull streams the subscriber will open
      // to receive message. It defaults to 1. `setExecutorProvider` configures an executor for the
      // subscriber to process messages. Here, the subscriber is configured to open 2 streams for
      // receiving messages, each stream creates a new executor with 4 threads to help process the
      // message callbacks. In total 2x4=8 threads are used for message processing.
      return Subscriber.newBuilder(getSampleSummaryActivationSubscriptionName(), receiver)
          .setParallelPullCount(2)
          .setExecutorProvider(executorProvider)
          .build();
    } else {
      LOG.info("Returning emulator subscriber for sample summary activation");
      return new PubSubEmulator().getEmulatorSubscriberForSampleSummaryActivation(receiver);
    }
  }

  /**
   * * Provides subscription name for the sample unit subscriber
   *
   * @return com.google.pubsub.v1.ProjectSubscriptionName
   */
  private ProjectSubscriptionName getSampleSummaryActivationSubscriptionName() {
    String project = appConfig.getGcp().getProject();
    String subscriptionId = appConfig.getGcp().getSampleSummaryActivationSubscription();
    LOG.info(
        "creating pubsub subscription name for sample summary activation",
        kv("Subscription id", subscriptionId),
        kv("project", project));
    return ProjectSubscriptionName.of(project, subscriptionId);
  }
}
