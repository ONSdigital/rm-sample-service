package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@MessageEndpoint
public class PartyReceiver {
  private static final Logger log = LoggerFactory.getLogger(PartyReceiver.class);

  @Autowired private SampleService sampleService;

  @ServiceActivator(inputChannel = "partyTransformed", adviceChain = "partyRetryAdvice")
  public void acceptParty(PartyCreationRequestDTO party) throws Exception {
    log.debug("acceptParty", kv("party", party));
    sampleService.sendToPartyService(party);
  }
}
