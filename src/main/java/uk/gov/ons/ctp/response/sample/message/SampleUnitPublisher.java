package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import net.sourceforge.cobertura.CoverageIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.response.sample.SampleSvcApplication;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

import javax.websocket.MessageHandler;

/** The publisher to queues */
@CoverageIgnore
@MessageEndpoint
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);


  @Autowired
  private PubSubConfig.PubsubOutboundGateway messagingGateway;

  /**
   * send sample to collection exercise queue
   *
   * @param sampleUnit to be sent
   */
  public void send(SampleUnit sampleUnit) {
    log.debug("send to queue sampleDelivery", kv("sample_unit", sampleUnit));
    messagingGateway.sendToPubsub(sampleUnit);
  }




}
