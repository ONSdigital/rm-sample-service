package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryActivationException;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryActivationService;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryEnrichmentService;

/** PubSub subscription responsible for sample summary activation via PubSub. */
@Component
public class SampleSummaryActivation {
  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryActivation.class);
  @Autowired private ObjectMapper objectMapper;

  @Autowired SampleSummaryActivationStatusPublisher sampleSummaryActivationStatusPublisher;

  @Autowired SampleSummaryActivationService sampleSummaryActivationService;

  @Autowired SampleSummaryEnrichmentService sampleSummaryEnrichmentService;

  /**
   * To process Sample summary activation from PubSub.
   *
   * @throws IOException
   */
  @ServiceActivator(inputChannel = "sampleSummaryActivationChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg) {
    LOG.info(
        "Receiving message ID from PubSub",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));

    String payload = new String((byte[]) message.getPayload());
    LOG.debug("Receiving data from PubSub ", kv("payload", payload));
    try {
      SampleSummaryActivationDTO sampleSummaryActivation =
          objectMapper.readValue(payload, SampleSummaryActivationDTO.class);

      // We ack here now that the message has be deserialized correctly.  If something goes
      // wrong then
      // we'll inform collection exercise via a separate pubsub message.
      pubSubMsg.ack();
      sampleSummaryActivationService.activateSampleSummaryFromPubsub(sampleSummaryActivation);

    } catch (final IOException e) {
      LOG.error(
          "Something went wrong while processing message received from PubSub "
              + "for sample unit notification",
          e);
      // Only nack on this specific if we can't deserialize it
      pubSubMsg.nack();
    } catch (final SampleSummaryActivationException e) {
      LOG.error("Something went wrong during the sample summary activation", e);
      // Intentionally not nacking as we've already told collection exercise of the
      // failure.
    }
  }
}
