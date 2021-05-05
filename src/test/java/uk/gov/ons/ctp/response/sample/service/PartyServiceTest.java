package uk.gov.ons.ctp.response.sample.service;

import libs.common.FixtureHelper;
import libs.party.representation.PartyDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.validation.BusinessSurveySample;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartyServiceTest {

    @Mock
    private PartySvcClientService partySvcClient;

    @InjectMocks
    private PartyService partyService;

    private List<PartyCreationRequestDTO> party;
    private List<PartyDTO> partyDTO;

    /**
     * Before the test
     *
     * @throws Exception oops
     */
    @Before
    public void setUp() throws Exception {
        party = FixtureHelper.loadClassFixtures(PartyCreationRequestDTO[].class);
        partyDTO = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    }

    /**
     * Test that when a Party is posted to Party Service the party svc client is called
     *
     * @throws Exception any exception fails the tests
     */
    @Test
    public void postPartyDTOToPartyServiceAndUpdateStatesTest() throws Exception {
        when(partySvcClient.postParty(any())).thenReturn(partyDTO.get(0));
        partyService.sendToPartyService(party.get(0));
        verify(partySvcClient).postParty(any(PartyCreationRequestDTO.class));
    }
}
