package uk.gov.ons.ctp.response.party.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;

@Path("/party")
@Produces({"application/json"})
@Slf4j
public class PartyEndpoint implements CTPEndpoint {

  @POST
  @Path("/events")
  public Response createCaseEvent(final PartyDTO partyDTO) throws CTPException {
    log.info(partyDTO.getForename());
    return Response.ok(partyDTO).status(Status.CREATED).build();

  }

  @GET
  @Path("/events")
  public Response up() {
    log.info("party up");
    return Response.ok().status(Status.CREATED).build();

  } 
  
}
