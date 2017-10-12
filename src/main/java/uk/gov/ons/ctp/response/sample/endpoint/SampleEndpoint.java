package uk.gov.ons.ctp.response.sample.endpoint;

import liquibase.util.csv.opencsv.bean.CsvToBean;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnitVerify;

import javax.validation.Valid;
import java.net.URI;

/**
 * The REST endpoint controller for Sample Service.
 */

@RestController
@RequestMapping(value = "/samples", produces = "application/json")
@Slf4j
public final class SampleEndpoint extends CsvToBean<BusinessSampleUnitVerify> {

  @Autowired
  private SampleService sampleService;

  @Autowired
  private MapperFacade mapperFacade;

  /**
   * POST CollectionExerciseJob associated to SampleSummary surveyRef and exerciseDateTime
   *
   * @param collectionExerciseJobCreationRequestDTO CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @param bindingResult collects errors thrown
   * @return Response Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(value = "/sampleunitrequests", method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<SampleUnitsRequestDTO> createSampleUnitRequest(final @Valid @RequestBody CollectionExerciseJobCreationRequestDTO
                                                       collectionExerciseJobCreationRequestDTO,
                                                                       BindingResult bindingResult) throws CTPException, InvalidRequestException {
    log.debug("Entering createCollectionExerciseJob with requestObject {}", collectionExerciseJobCreationRequestDTO);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for create action: ", bindingResult);
    }

    CollectionExerciseJob cej = mapperFacade.map(collectionExerciseJobCreationRequestDTO, CollectionExerciseJob.class);
    cej.setCreatedDateTime(DateTimeUtil.nowUTC());

    Integer sampleUnitsTotal = sampleService.initialiseCollectionExerciseJob(cej);
    SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO(sampleUnitsTotal);

    String newResourceUrl = ServletUriComponentsBuilder
        .fromCurrentRequest().path("/{id}")
        .buildAndExpand(cej.getCollectionExerciseId()).toUri().toString();

    return ResponseEntity.created(URI.create(newResourceUrl)).body(sampleUnitsRequest);
  }

  @RequestMapping(value = "/{type}/fileupload", method = RequestMethod.POST, consumes = "multipart/form-data")
  public final ResponseEntity<SampleSummary> uploadSampleFile(@PathVariable("type") final String type, @RequestParam("file") MultipartFile file) throws CTPException {
    log.debug("Entering Sample file upload for Type {}", type);

    SampleSummary sampleSummary;
    try {
      sampleSummary = sampleService.ingest(file);
    } catch (Exception e) {
      throw new CTPException(CTPException.Fault.VALIDATION_FAILED, e, "Error ingesting file %s", file.getOriginalFilename());
    }

    return ResponseEntity.created(URI.create("TODO")).body(sampleSummary);
  }

}
