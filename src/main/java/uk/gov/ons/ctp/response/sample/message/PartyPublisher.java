package uk.gov.ons.ctp.response.sample.message;

import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;

/**
 * Service responsible for publishing Party(s) to queue Sample.Party
 */
public interface PartyPublisher {

    /**
     * To put one Party on the queue Sample.Party
     *
     * @param party the party to put on the queue
     */
    void publish(PartyCreationRequestDTO party);

}
