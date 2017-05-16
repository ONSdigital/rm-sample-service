package uk.gov.ons.ctp.response.sample.message;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.message.impl.PartyPublisherImpl;

/**
 * To unit test CaseReceiptReceiverImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class SendToPartyImplTest {

	 @InjectMocks
	 PartyPublisherImpl sendToParty;
	 
	 @Mock 
	 RabbitTemplate rabbitTemplate;
 
  @Test
  public void testSendToPartyAddsToThePartyQueue() {
    Party partyDTO = new Party();
    partyDTO.setId("1");
    partyDTO.setPosition(1);
    partyDTO.setSampleId(123);
    partyDTO.setSampleUnitRef("str123");
    partyDTO.setSampleUnitType("H");
    partyDTO.setSize(1);
    
    sendToParty.publish(partyDTO);
    
    verify(rabbitTemplate, times(1)).convertAndSend(partyDTO);
  }
  
}
