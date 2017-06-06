package uk.gov.ons.ctp.response.party.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.party.definition.Party;

/**
 * The Mock REST endpoint controller for Party Service.
 */
@RestController
@RequestMapping(value = "/party", produces = "application/json")
@Slf4j
public class PartyEndpoint implements CTPEndpoint {

  @Autowired
  @Qualifier("sampleServiceClient")
  private RestClient sampleServiceClient;

  @Qualifier("sampleBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * POST to update state for a specified PartyDTO.
   *
   * @param partyDTO partyDTO to be updated
   * @throws CTPException if update operation fails
   * @return Response PartyDTO that has been updated
   */
  @RequestMapping(value = "/events", method = RequestMethod.POST, consumes = "application/xml")
  public ResponseEntity<?> createCaseEvent(final Party partyDTO) throws CTPException {

    log.debug(partyDTO.getPosition() + " / " + partyDTO.getSize());
    log.debug(Integer.toString(partyDTO.getSampleId()));
 //   log.debug(Boolean.toString(sampleServiceClient == null));
    /*if (partyDTO.getPosition() == partyDTO.getSize()) {
      sampleServiceClient.putResource("/samples/" + partyDTO.getSampleId(), null, null, partyDTO.getSampleId());
      return Response.ok(partyDTO).status(Status.OK).build();
    }*/
    return ResponseEntity.ok(partyDTO);
  }

}
