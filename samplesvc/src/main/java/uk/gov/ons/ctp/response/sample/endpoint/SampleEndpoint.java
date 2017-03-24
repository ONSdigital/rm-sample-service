package uk.gov.ons.ctp.response.sample.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * The REST endpoint controller for Internet Access Codes.
 */
@Path("/iacs")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Slf4j
public final class SampleEndpoint implements CTPEndpoint {
  
  @Inject
  private AppConfig appConfig;

  @Inject
  private SampleService sampleService;


  @Inject
  private MapperFacade mapperFacade;

//  @GET
//  @Path("/{iac}")
//  public final Response getIACCaseContext(@PathParam("iac") final String iac) throws CTPException {
//    log.info("Entering getIACCaseContext for {}", iac);
//    String internalIAC = InternetAccessCodeFormatter.internalize(iac);
//    InternetAccessCodeCaseContext iacCaseContext = internetAccessCodeService.getCaseContext(internalIAC);
//
//    return Response.ok(mapperFacade.map(iacCaseContext, InternetAccessCodeCaseContextDTO.class)).build();
//  }
//
//  /**
//   * Deactivate a given iac entry in the db by setting its active flag to false
//   * @param iac identifies the IAC row in the db
//   * @param updatedBy who did this
//   * @return the updated IAC record
//   * @throws CTPException failed to find the given IAC in the db
//   */
//  @PUT
//  @Path("/{iac}")
//  public final Response deactivateIAC(@PathParam("iac") final String iac,
//      final UpdateInternetAccessCodeDTO updateRequest) throws CTPException {
//    log.info("Entering deactivateIAC with iac {} - updatedBy {}", iac, updateRequest.getUpdatedBy());
//    String internalIAC = InternetAccessCodeFormatter.internalize(iac);
//    InternetAccessCode internetAccessCode = internetAccessCodeService.deactivateCode(internalIAC, updateRequest.getUpdatedBy());
//    if (internetAccessCode == null) {
//      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, "IAC not found for code %s", iac);
//    }
//    return Response.ok(mapperFacade.map(internetAccessCode, InternetAccessCodeDTO.class)).build();
//  }
//
//  /**
//   * Create a number of IAC codes on request.
//   * @param createRequest contains the count and who requested the creation
//   * @return the list of IAC codes
//   * @throws CTPException Defined creation count max exceeded
//   */
//  @POST
//  @Path("/")
//  public final Response generateCodes(CreateInternetAccessCodeDTO createRequest) throws CTPException {
//    log.info("Entering generateCodes with count {}, createdBy {}", createRequest.getCount(),
//        createRequest.getCreatedBy());
//
//    if (createRequest.getCount() > appConfig.getMaxIac()) {
//      throw new CTPException(CTPException.Fault.VALIDATION_FAILED, "Requested IAC count of %d exceeds system limit of %d", createRequest.getCount(), appConfig.getMaxIac());
//    }
//
//    List<InternetAccessCode> codes = internetAccessCodeService.generateCodes(createRequest.getCount(), createRequest.getCreatedBy());
//
//    return Response.ok(codes.stream()
//        .map(iac->InternetAccessCodeFormatter.externalize(iac.getCode()))
//        .toArray(String[]::new)).status(Status.CREATED).build();
//  }
}
