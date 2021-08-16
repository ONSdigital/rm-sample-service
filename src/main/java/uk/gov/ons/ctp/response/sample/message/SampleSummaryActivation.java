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
import java.util.UUID;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseStatusDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;
import uk.gov.ons.ctp.response.sample.service.NoSampleUnitsInSampleSummaryException;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryDistributionService;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryEnrichmentService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;
import uk.gov.ons.ctp.response.sample.utility.PubSubEmulator;

/** PubSub subscription responsible for receipt of sample units via PubSub. */
@Component
public class SampleSummaryActivation {
  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryActivation.class);
  @Autowired private ObjectMapper objectMapper;
  @Autowired AppConfig appConfig;
  @Autowired SampleSummaryEnrichmentService sampleSummaryEnrichmentService;
  @Autowired SampleSummaryDistributionService sampleSummaryDistributionService;

  /**
   * To process SampleUnit from PubSub This creates application ready event listener to provide an
   * active subscription for the new sample unit when published.
   *
   * @throws IOException
   */
  @EventListener(ApplicationReadyEvent.class)
  public void activateSampleSummary() throws IOException {
    LOG.debug("received SampleSummaryActivation message from PubSub");
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

            // Figure out when it should ack/nack at the end, once we know of all the places
            // it can go wrong.
            activateSampleSummaryFromPubsub(sampleSummaryActivation);
            consumer.ack();

          } catch (final IOException e) {
            LOG.error(
                "Something went wrong while processing message received from PubSub "
                    + "for sample unit notification",
                e);
            consumer.nack();
          }
        };
    Subscriber subscriber = getSampleSummaryActivationSubscriber(receiver);
    // Start the subscriber.
    subscriber.startAsync().awaitRunning();
    LOG.info(
        "Listening for sample unit notification messages on PubSub-subscription id",
        kv("subscriptionId", subscriber.getSubscriptionNameString()));
  }

  private void activateSampleSummaryFromPubsub(SampleSummaryActivationDTO sampleSummaryActivation) {
    validateAndEnrich(sampleSummaryActivation);
    CollectionExerciseStatusDTO collectionExerciseStatus = new CollectionExerciseStatusDTO();
    collectionExerciseStatus.setCollectionExerciseId(
        sampleSummaryActivation.getCollectionExerciseId());
    // sendMessageToCollectionExercise(collectionExerciseStatus)
    // Send message about 'enrichment complete' to collection exercise
    distribute(sampleSummaryActivation);
    // Send message about 'distribution complete' to collection exercise

  }

  private void validateAndEnrich(SampleSummaryActivationDTO sampleSummaryActivation) {
    UUID sampleSummaryId = sampleSummaryActivation.getSampleSummaryId();
    UUID surveyId = sampleSummaryActivation.getSurveyId();
    UUID collectionExerciseId = sampleSummaryActivation.getCollectionExerciseId();

    LOG.debug(
        "about to enrich sample summary",
        kv("sampleSummaryId", sampleSummaryId),
        kv("surveyId", surveyId),
        kv("collectionExerciseId", collectionExerciseId));

    try {
      boolean validated =
          sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);
      LOG.debug(
          "Enriched sample summary",
          kv("sampleSummaryId", sampleSummaryId),
          kv("surveyId", surveyId),
          kv("collectionExerciseId", collectionExerciseId),
          kv("validated", validated));
      if (validated) {
        LOG.info("Success!");
      } else {
        LOG.error("TODO - do something when something goes wrong with validation and enrichment");
        // TODO do something useful on failure
      }
    } catch (UnknownSampleSummaryException e) {
      LOG.error("unknown sample summary id", kv("sampleSummaryId", sampleSummaryId), e);
      // TODO do something useful on failure
    }
  }

  private void distribute(SampleSummaryActivationDTO sampleSummaryActivation) {
    try {
      sampleSummaryDistributionService.distribute(sampleSummaryActivation.getSampleSummaryId());
    } catch (NoSampleUnitsInSampleSummaryException | UnknownSampleSummaryException e) {
      LOG.error("TODO - something went wrong");
      // TODO - do something useful when it goes wrong
    }
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
      return Subscriber.newBuilder(getSampleUnitSubscriptionName(), receiver)
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
  private ProjectSubscriptionName getSampleUnitSubscriptionName() {
    String project = appConfig.getGcp().getProject();
    String subscriptionId = appConfig.getGcp().getSampleSummaryActivationSubscription();
    LOG.info(
        "creating pubsub subscription name for sample unit notifications",
        kv("Subscription id", subscriptionId),
        kv("project", project));
    return ProjectSubscriptionName.of(project, subscriptionId);
  }
}
