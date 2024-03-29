package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.utility.PubSubEmulator;

@Component
public class PubSub {
  private static final Logger log = LoggerFactory.getLogger(PubSub.class);
  @Autowired AppConfig appConfig;

  private Publisher publisherSupplier(String project, String topic) throws IOException {
    log.info("creating pubsub publish for topic " + topic + " in project " + project);
    TopicName topicName = TopicName.of(project, topic);
    if (StringUtil.isBlank(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      log.info("Returning actual Publisher");
      return Publisher.newBuilder(topicName).build();
    } else {
      log.info(
          "Returning emulator Publisher",
          kv("PubSub emulator host", System.getenv("PUBSUB_EMULATOR_HOST")));
      return new PubSubEmulator().getEmulatorPublisher(topicName);
    }
  }

  public Publisher caseNotificationPublisher() throws IOException {
    return publisherSupplier(
        appConfig.getGcp().getProject(), appConfig.getGcp().getCaseNotificationTopic());
  }

  public void shutdown() {
    if (StringUtil.isEmpty(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      PubSubEmulator.CHANNEL.shutdown();
    }
  }

  /**
   * * Provides subscription name for the sample file dead letter subscriber
   *
   * @return com.google.pubsub.v1.ProjectSubscriptionName
   */
  public ProjectSubscriptionName getSampleDeadLetterSubscriptionName() {
    String project = appConfig.getGcp().getProject();
    String subscriptionId = appConfig.getGcp().getSampleSummaryDeadLetterSubscription();
    log.info(
        "creating pubsub subscription name for sample dead letter "
            + subscriptionId
            + " in project "
            + project);
    return ProjectSubscriptionName.of(project, subscriptionId);
  }

  /**
   * Provides PubSub subscriber for sample summary notification against message receiver
   *
   * @param receiver: com.google.cloud.pubsub.v1.MessageReceiver;
   * @return com.google.cloud.pubsub.v1.Subscriber;
   */
  public Subscriber getSampleDeadLetterSubscriber(MessageReceiver receiver) throws IOException {
    if (null == System.getenv("PUBSUB_EMULATOR_HOST")) {
      log.info("Returning Subscriber");
      ExecutorProvider executorProvider =
          InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4).build();
      // `setParallelPullCount` determines how many StreamingPull streams the subscriber will open
      // to receive message. It defaults to 1. `setExecutorProvider` configures an executor for the
      // subscriber to process messages. Here, the subscriber is configured to open 2 streams for
      // receiving messages, each stream creates a new executor with 4 threads to help process the
      // message callbacks. In total 2x4=8 threads are used for message processing.
      return Subscriber.newBuilder(getSampleDeadLetterSubscriptionName(), receiver)
          .setParallelPullCount(2)
          .setExecutorProvider(executorProvider)
          .build();
    } else {
      log.info("Returning emulator Subscriber");
      return new PubSubEmulator().getEmulatorSubscriber(receiver);
    }
  }
}
