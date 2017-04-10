package uk.gov.ons.ctp.response.sample.endpoint;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
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
  
  @PUT
  @Path("/{sampleId}")
  public final void activateSampleSummary(@PathParam("sampleId") final Integer sampleId) throws CTPException {
    
    sampleService.activateSampleSummaryState(sampleId);
    
  }
  
  @GET
  @Path("/{surveyRef}/{exerciseDateTime}")
  public Response getSampleSummary(@PathParam("surveyRef") final String surveyRef, @PathParam("exerciseDateTime") final Timestamp exerciseDateTime) throws CTPException {
    
    List<SampleUnit> listSampleUnits = sampleService.findSampleUnitsBySurveyRefandExerciseDateTime(surveyRef, exerciseDateTime);

    ResponseBuilder responseBuilder = Response.ok(CollectionUtils.isEmpty(listSampleUnits) ? null : listSampleUnits);
    responseBuilder.status(CollectionUtils.isEmpty(listSampleUnits) ? Status.NO_CONTENT : Status.OK);
    return responseBuilder.build();
    
  }
  
}
