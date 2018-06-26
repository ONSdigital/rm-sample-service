package uk.gov.ons.ctp.response.sample.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;

@Slf4j
@MessageEndpoint
public class PartyPublisherImpl implements PartyPublisher {

  @Qualifier("partyRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Override
  public void publish(PartyCreationRequestDTO party) {
    log.debug("send to queue to be sent to partySvc {}", party);
    rabbitTemplate.convertAndSend(party);
  }
}
