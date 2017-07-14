package uk.gov.ons.ctp.response.sample.message.impl;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.message.PartyPostingReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * Created by wardlk on 06/07/2017.
 */
public class PartyPostingReceiverImpl implements PartyPostingReceiver {

    @Autowired
    private SampleService sampleService;

    //@ServiceActivator(inputChannel = "partyTransformed", adviceChain = "partyRetryAdvice")
    public void acceptParty(Party party) throws Exception {
        sampleService.sendToPartyService(party);
    }
}
