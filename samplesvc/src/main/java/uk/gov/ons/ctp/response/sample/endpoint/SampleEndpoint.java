package uk.gov.ons.ctp.response.sample.endpoint;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
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

    log.debug("Activating SampleId: " + sampleId);
    SampleSummary sampleSummary = sampleService.activateSampleSummaryState(sampleId);

    return Response.ok(mapperFacade.map(sampleSummary, SampleSummaryDTO.class)).build();

  }

  /**
   * POST CollectionExerciseJob associated to SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param collectionExerciseJobCreationRequestDTO CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Response Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  @POST
  @Path("/sampleunitrequests")
  public Response getSampleSummary(
      @Valid final CollectionExerciseJobCreationRequestDTO collectionExerciseJobCreationRequestDTO)
          throws CTPException {

    log.info("Entering createCollectionExerciseJob with requestObject {}", collectionExerciseJobCreationRequestDTO);

    Integer sampleUnitsTotal = sampleService.initialiseCollectionExerciseJob(collectionExerciseJobCreationRequestDTO);

    SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO(sampleUnitsTotal);

    return Response.ok(mapperFacade.map(sampleUnitsRequest, SampleUnitsRequestDTO.class)).build();

  }

}
