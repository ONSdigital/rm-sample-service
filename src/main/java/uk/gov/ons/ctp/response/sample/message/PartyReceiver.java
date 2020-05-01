package uk.gov.ons.ctp.response.sample.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import libs.party.definition.PartyCreationRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@MessageEndpoint
public class PartyReceiver {
  private static final Logger log = LoggerFactory.getLogger(PartyReceiver.class);

  @Autowired private SampleService sampleService;

  @ServiceActivator(inputChannel = "partyTransformed", adviceChain = "partyRetryAdvice")
  public void acceptParty(PartyCreationRequestDTO party) throws Exception {
    log.with("party", party).debug("acceptParty");
    sampleService.sendToPartyService(party);
  }
}
