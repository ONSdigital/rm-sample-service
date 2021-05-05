package uk.gov.ons.ctp.response.sample.service;

import libs.common.error.CTPException;
import libs.party.representation.PartyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;

@Service
public class PartyService {

    @Autowired
    private PartySvcClientService partySvcClient;

    @Async
    public void sendToPartyService(PartyCreationRequestDTO partyCreationRequest)
            throws CTPException {
        partySvcClient.postParty(partyCreationRequest);
    }
}
