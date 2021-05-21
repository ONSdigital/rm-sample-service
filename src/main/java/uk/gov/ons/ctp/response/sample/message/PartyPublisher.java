package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;

@MessageEndpoint
public class PartyPublisher {
  private static final Logger log = LoggerFactory.getLogger(PartyPublisher.class);

  @Qualifier("partyRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  public void publish(PartyCreationRequestDTO party) {
    log.debug("send to queue to be sent to partySvc", kv("party", party));
    rabbitTemplate.convertAndSend(party);
  }
}
