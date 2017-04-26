package uk.gov.ons.ctp.response.sample.message.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.message.SendToParty;
/**
 * The publisher to queues
 */
@Slf4j
@Named
public class SendToPartyImpl implements SendToParty {

  @Qualifier("partyRabbitTemplate")
  @Inject
  private RabbitTemplate rabbitTemplate;

  @Override
  public void send(Party partyDTO) {
    log.debug("send to queue partysvc {}", partyDTO);
    rabbitTemplate.convertAndSend(partyDTO);
  }
}
