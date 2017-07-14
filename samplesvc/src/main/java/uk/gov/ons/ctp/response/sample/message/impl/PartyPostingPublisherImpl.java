package uk.gov.ons.ctp.response.sample.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.message.PartyPostingPublisher;

/**
 * Created by wardlk on 06/07/2017.
 */
@Slf4j
@Component
public class PartyPostingPublisherImpl implements PartyPostingPublisher {

  @Qualifier("partyRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

    @Override
    public void publish(Party party) {
        log.debug("send to queue to be sent to party {}", party);
           rabbitTemplate.convertAndSend(party);
         }
}
