package uk.gov.ons.ctp.response.sample.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import libs.party.definition.PartyCreationRequestDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;

@MessageEndpoint
public class PartyPublisher {
  private static final Logger log = LoggerFactory.getLogger(PartyPublisher.class);

  @Qualifier("partyRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  public void publish(PartyCreationRequestDTO party) {
    log.with("party", party).debug("send to queue to be sent to partySvc");
    rabbitTemplate.convertAndSend(party);
  }
}
