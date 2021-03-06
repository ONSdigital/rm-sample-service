package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.google.cloud.pubsub.v1.Publisher;
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

  public Publisher sampleUnitPublisher() throws IOException {
    return publisherSupplier(
        appConfig.getGcp().getProject(), appConfig.getGcp().getSampleUnitPublisherTopic());
  }

  public void shutdown() {
    if (StringUtil.isEmpty(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      PubSubEmulator.CHANNEL.shutdown();
    }
  }
}
