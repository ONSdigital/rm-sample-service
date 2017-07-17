package uk.gov.ons.ctp.response.sample.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.party.representation.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;

/**
 * Created by wardlk on 20/06/2017.
 */
@Slf4j
@Service
public class PartySvcClientServiceImpl implements PartySvcClientService {
    @Autowired
    private AppConfig appConfig;

    @Autowired
    @Qualifier("partySvcClient")
    private RestClient partySvcClient;

    @Override
    public PartyDTO postParty(final PartyCreationRequestDTO newPartyDTO) {
        PartyDTO party = partySvcClient.postResource(appConfig.getPartySvc().getPostPartyPath(),
                newPartyDTO, PartyDTO.class);
        log.debug("PARTY GOTTEN: " + party.toString());
        return party;
    }
}
