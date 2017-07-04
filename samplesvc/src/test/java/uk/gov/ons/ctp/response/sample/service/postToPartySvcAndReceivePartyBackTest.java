package uk.gov.ons.ctp.response.sample.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.party.representation.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.PartySvc;
import uk.gov.ons.ctp.response.sample.service.impl.PartySvcClientServiceImpl;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by wardlk on 04/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class postToPartySvcAndReceivePartyBackTest {



    @InjectMocks
    private PartySvcClientServiceImpl partySvcClientService;

    @Mock
    private RestClient partySvcClient;

    @Mock
    private AppConfig appConfig;

    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyPartyPostedTo() throws CTPException {
        PartyDTO party = new PartyDTO();
        PartyCreationRequestDTO newParty = new PartyCreationRequestDTO();

        PartySvc partySvc = new PartySvc();
        partySvc.setPostPartyPath("/path");

        when(appConfig.getPartySvc()).thenReturn(partySvc);
        when(partySvcClient.postResource(any(), any(), any())).thenReturn(party);

        partySvcClientService.postParty(newParty);

        verify(partySvcClient, times(1)).postResource(partySvc.getPostPartyPath(), newParty, PartyDTO.class);
    }
}
