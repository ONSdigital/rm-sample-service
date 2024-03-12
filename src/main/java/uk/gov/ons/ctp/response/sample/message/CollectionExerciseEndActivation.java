package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.io.IOException;
import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseEndEventDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseEndService;

@Component
public class CollectionExerciseEndActivation {

  private static final Logger LOG = LoggerFactory.getLogger(CollectionExerciseEndActivation.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CollectionExerciseEndService collectionExerciseEndService;

  /** To process Collection Exercise End event from PubSub. */
  @ServiceActivator(inputChannel = "collectionExerciseEndActivationChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {
    LOG.info(
        "Receiving Collection Exercise End event",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));

    String payload = new String((byte[]) message.getPayload());

    try {
      CollectionExerciseEndEventDTO collectionExerciseEndEventDTO =
          objectMapper.readValue(payload, CollectionExerciseEndEventDTO.class);

      collectionExerciseEndService.collectionExerciseEnd(
          collectionExerciseEndEventDTO.getCollectionExerciseId());

      pubSubMsg.ack();

    } catch (IOException | CTPException e) {
      LOG.error(
          "Something went wrong while processing message received from PubSub "
              + "for Collection Exercise End event",
          kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()),
          e);
      pubSubMsg.nack();
    }
  }
}
