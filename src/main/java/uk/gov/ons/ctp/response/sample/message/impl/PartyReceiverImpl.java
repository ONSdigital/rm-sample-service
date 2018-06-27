package uk.gov.ons.ctp.response.sample.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.message.PartyReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@Slf4j
@MessageEndpoint
public class PartyReceiverImpl implements PartyReceiver {

  @Autowired private SampleService sampleService;

  @ServiceActivator(inputChannel = "partyTransformed", adviceChain = "partyRetryAdvice")
  public void acceptParty(PartyCreationRequestDTO party) throws Exception {
    log.debug("acceptParty {}", party);
    sampleService.sendToPartyService(party);
  }
}
