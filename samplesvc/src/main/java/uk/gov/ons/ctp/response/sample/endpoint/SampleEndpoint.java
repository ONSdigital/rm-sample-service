package uk.gov.ons.ctp.response.sample.endpoint;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * The REST endpoint controller for Sample Service.
 */
@Path("/samples")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Slf4j
public final class SampleEndpoint implements CTPEndpoint {

  @Inject
  private SampleService sampleService;

  @Inject
  private MapperFacade mapperFacade;

  /**
   * PUT to update state for a specified SampleSummary.
   *
   * @param sampleId SampleId of the SampleSummary to update.
   * @return SampleSummary Returns the updated SampleSummary
   * @throws CTPException if update operation fails
   */
  @PUT
  @Path("/{sampleid}")
  public Response activateSampleSummary(@PathParam("sampleid") final Integer sampleId) throws CTPException {

    log.info("Activating SampleId: " + sampleId);
    
    SampleSummary sampleSummary = sampleService.activateSampleSummaryState(sampleId);

    return Response.ok(mapperFacade.map(sampleSummary, SampleSummaryDTO.class)).build();

  }

  /**
   * GET List of Sample Units by parent SampleSummary surveyRef and exerciseDateTime
   *
   * @param surveyRef surveyRef to which SampleUnits are related
   * @param exerciseDateTime exerciseDateTime to which SampleUnits are related
   * @return List<SampleUnit> Returns the associated SampleUnits for the specified surveyRef and exerciseDateTime.
   * @throws CTPException if update operation fails
   */
  @GET
  @Path("/{surveyref}/{exercisedatetime}")
  public Response getSampleSummary(@PathParam("surveyref") final String surveyRef, @PathParam("exercisedatetime") final Timestamp exerciseDateTime) throws CTPException {

    List<SampleUnit> listSampleUnits = sampleService.findSampleUnitsBySurveyRefandExerciseDateTime(surveyRef, exerciseDateTime);

    ResponseBuilder responseBuilder = Response.ok(CollectionUtils.isEmpty(listSampleUnits) ? null : listSampleUnits);
    responseBuilder.status(CollectionUtils.isEmpty(listSampleUnits) ? Status.NO_CONTENT : Status.OK);
    return responseBuilder.build();

  }

}
