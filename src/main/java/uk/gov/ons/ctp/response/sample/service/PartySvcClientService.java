package uk.gov.ons.ctp.response.sample.service;

import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

/**
 * A Service which utilises the PartySvc via RESTful client calls
 *
 */
public interface PartySvcClientService {

    /**
     * Call PartySvc using REST to get the Party. It MAY throw a RuntimeException if the call fails.
     *
     * @param partyCreationRequestDTO the partyCreationRequestDTO
     * @return the Party we created/fetched
     */
    PartyDTO postParty(PartyCreationRequestDTO partyCreationRequestDTO);
}
