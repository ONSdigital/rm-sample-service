package uk.gov.ons.ctp.response.sample.endpoint;

import java.sql.Timestamp;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
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
  private CollectionExerciseJobService collectionExerciseJobService;
  
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

    log.debug("Activating SampleId: " + sampleId);
    SampleSummary sampleSummary = sampleService.activateSampleSummaryState(sampleId);

    return Response.ok(mapperFacade.map(sampleSummary, SampleSummaryDTO.class)).build();

  }

  /**
   * GET List of Sample Units by parent SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param surveyRef surveyRef to which SampleUnits are related
   * @param exerciseDateTime exerciseDateTime to which SampleUnits are related
   * @return List<SampleUnit> Returns the associated SampleUnits for the
   *         specified surveyRef and exerciseDateTime.
   * @throws CTPException if update operation fails
   */
  @POST
  @Path("/sampleunitrequests")
  public Response getSampleSummary(@QueryParam("collectionExerciseId") final Integer collectionExerciseId,
      @QueryParam("surveyRef") final String surveyRef, 
      @QueryParam("exerciseDateTime") final Timestamp exerciseDateTime) throws CTPException {
    
    /*
     * TODO: GET currently only works with exerciseDateTime in this format: 2012-12-13%2012:12:12
     * exerciseDateTime format has not yet been specified so work with this for now.
     */
    
    Integer sampleUnitsTotal = sampleService.findSampleUnitsSize(surveyRef, exerciseDateTime);

    SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO();
    sampleUnitsRequest.setSampleUnitsTotal(sampleUnitsTotal);

    if (sampleUnitsTotal != 0) {
      CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
      collectionExerciseJob.setCollectionExerciseId(collectionExerciseId);
      collectionExerciseJob.setSurveyRef(surveyRef);
      collectionExerciseJob.setExerciseDateTime(exerciseDateTime);
      collectionExerciseJob.setCreatedDateTime(DateTimeUtil.nowUTC());
      
      collectionExerciseJobService.processCollectionExerciseJob(collectionExerciseJob);
    }

    return Response.ok(mapperFacade.map(sampleUnitsRequest, SampleUnitsRequestDTO.class)).build();

  }

}
