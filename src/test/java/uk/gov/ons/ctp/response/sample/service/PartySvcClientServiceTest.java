package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.PartySvc;
import uk.gov.ons.ctp.response.sample.service.impl.PartySvcClientServiceImpl;

/**
 * tests
 */
@RunWith(MockitoJUnitRunner.class)
public class PartySvcClientServiceTest {

  @InjectMocks
  private PartySvcClientServiceImpl partySvcClientService;

  @Mock
  private RestClient partySvcClient;

  @Mock
  private AppConfig appConfig;

  /**
   * Set up tests
   *
   * @throws Exception oops
   */
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * verify post to party
   *
   * @throws CTPException oops
   */
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
