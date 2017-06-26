package uk.gov.ons.ctp.response.sample.service;

import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.party.representation.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

import java.util.UUID;

/**
 * A Service which utilises the CaseSvc via RESTful client calls
 *
 */
public interface PartySvcClientService {

    /**
     * Call PartySvc using REST to get the Party MAY throw a RuntimeException if
     * the call fails
     *
     * @param party the PartySvc UUID
     * @return the Party we fetched!
     */
    PartyDTO postParty(PartyCreationRequestDTO newPartyDTO);
}
