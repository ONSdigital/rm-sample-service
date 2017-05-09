package uk.gov.ons.ctp.response.sample.endpoint;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * The REST endpoint controller for Sample Service.
 */

@RestController
@RequestMapping(value = "/samples", produces = "application/json")
@Slf4j
public final class SampleEndpoint implements CTPEndpoint {

  @Autowired
  private SampleService sampleService;

  @Qualifier("sampleBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /*
   * POST CollectionExerciseJob associated to SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param collectionExerciseJobCreationRequestDTO CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Response Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  @RequestMapping(value = "/sampleunitrequests", method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<?>  getSampleSummary(final @RequestBody CollectionExerciseJobCreationRequestDTO
                                                       collectionExerciseJobCreationRequestDTO,
                                             BindingResult bindingResult)throws CTPException {
    log.debug("Entering createCollectionExerciseJob with requestObject {}", collectionExerciseJobCreationRequestDTO);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for create action: ", bindingResult);
    }
    Integer sampleUnitsTotal = sampleService.initialiseCollectionExerciseJob(collectionExerciseJobCreationRequestDTO);
    SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO(sampleUnitsTotal);
    
    return ResponseEntity.created(URI.create("TODO")).body(sampleUnitsRequest);
  }

}
