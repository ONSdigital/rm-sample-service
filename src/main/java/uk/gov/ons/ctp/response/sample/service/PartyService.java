package uk.gov.ons.ctp.response.sample.service;

import libs.common.error.CTPException;
import libs.party.representation.PartyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;

import java.util.UUID;

@Service
public class PartyService {

    @Autowired
    private PartySvcClientService partySvcClient;

    @Autowired
    SampleUnitRepository sampleUnitRepository;

    @Async
    public void sendToPartyService(String sampleUnitId, PartyCreationRequestDTO partyCreationRequest)
            throws CTPException {
        PartyDTO party = partySvcClient.postParty(partyCreationRequest);
        SampleUnit sampleUnit =
                sampleUnitRepository.findById(
                        UUID.fromString(sampleUnitId)).orElseThrow();

        addPartyIdToSample(sampleUnit, party);
    }

    private void addPartyIdToSample(SampleUnit sampleUnit, PartyDTO party) throws CTPException {
        UUID partyId = UUID.fromString(party.getId());
        sampleUnit.setPartyId(partyId);
        sampleUnitRepository.saveAndFlush(sampleUnit);
    }
}
