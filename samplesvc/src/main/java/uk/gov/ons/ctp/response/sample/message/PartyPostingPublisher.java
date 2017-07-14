package uk.gov.ons.ctp.response.sample.message;

import uk.gov.ons.ctp.response.party.definition.Party;

/**
 * Created by wardlk on 06/07/2017.
 */
public interface PartyPostingPublisher {

    void publish(Party party);

}
