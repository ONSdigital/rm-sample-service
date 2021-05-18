package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import libs.common.FixtureHelper;
import libs.party.representation.PartyDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;

@RunWith(MockitoJUnitRunner.class)
public class PartyServiceTest {

  private static final String SAMPLEUNIT_ID = "4ef7326b-4143-43f7-ba67-65056d4e20b8";

  @Mock private PartySvcClientService partySvcClient;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @InjectMocks private PartyService partyService;

  private List<PartyCreationRequestDTO> party;
  private List<PartyDTO> partyDTO;
  private List<SampleUnit> sampleUnit;

  /**
   * Before the test
   *
   * @throws Exception oops
   */
  @Before
  public void setUp() throws Exception {
    party = FixtureHelper.loadClassFixtures(PartyCreationRequestDTO[].class);
    partyDTO = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    sampleUnit = FixtureHelper.loadClassFixtures(SampleUnit[].class);
  }

  /**
   * Test that when a Party is posted to Party Service the party svc client is called
   *
   * @throws Exception any exception fails the tests
   */
  @Test
  public void postPartyDTOToPartyServiceAndUpdateStatesTest() throws Exception {
    when(partySvcClient.postParty(any())).thenReturn(partyDTO.get(0));
    when(sampleUnitRepository.findById(UUID.fromString(SAMPLEUNIT_ID)))
        .thenReturn(Optional.of(sampleUnit.get(0)));

    CompletableFuture<Void> psc = partyService.sendToPartyService(SAMPLEUNIT_ID, party.get(0));
    psc.get();

    verify(partySvcClient).postParty(any(PartyCreationRequestDTO.class));
    verify(sampleUnitRepository).findById(UUID.fromString(SAMPLEUNIT_ID));
  }
}
