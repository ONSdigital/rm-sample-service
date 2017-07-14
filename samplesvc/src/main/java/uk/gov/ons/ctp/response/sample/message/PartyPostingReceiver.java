package uk.gov.ons.ctp.response.sample.message;

import uk.gov.ons.ctp.response.party.definition.Party;

/**
 * Created by wardlk on 11/07/2017.
 */
public interface PartyPostingReceiver {

    void acceptParty(Party party) throws Exception;
}
