package uk.gov.ons.ctp.response.sample.endpoint;

import liquibase.util.csv.opencsv.bean.CsvToBean;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnit;

/**
 * The REST endpoint controller for Sample Service.
 */

@RestController
@RequestMapping(value = "/samples", produces = "application/json")
@Slf4j
public final class SampleEndpoint extends CsvToBean<BusinessSampleUnit> {

  @Autowired
  private SampleService sampleService;

  @Autowired
  private MapperFacade mapperFacade;

  /**
   * POST CollectionExerciseJob associated to SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param collectionExerciseJobCreationRequestDTO
   *          CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @param bindingResult collects errors thrown
   * @return Response Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob
   *           already exists
   * @throws InvalidRequestException if binding errors
   */
  @RequestMapping(value = "/sampleunitrequests", method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<SampleUnitsRequestDTO> createSampleUnitRequest(
      final @Valid @RequestBody CollectionExerciseJobCreationRequestDTO collectionExerciseJobCreationRequestDTO,
      BindingResult bindingResult) throws CTPException, InvalidRequestException {
    log.debug("Entering createCollectionExerciseJob with requestObject {}", collectionExerciseJobCreationRequestDTO);
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for create action: ", bindingResult);
    }
    CollectionExerciseJob cej = new CollectionExerciseJob();
    Integer sampleUnitsTotal = 0;
    List<UUID> sampleSummaryIds = collectionExerciseJobCreationRequestDTO.getSampleSummaryUUIDList();
    for(UUID sampleSummaryID : sampleSummaryIds) {
      cej = mapperFacade
          .map(collectionExerciseJobCreationRequestDTO, CollectionExerciseJob.class);
      cej.setCreatedDateTime(DateTimeUtil.nowUTC());
      cej.setSampleSummaryId(sampleSummaryID);

      sampleUnitsTotal += sampleService.initialiseCollectionExerciseJob(cej);
    }
      SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO(sampleUnitsTotal);

      String newResourceUrl = ServletUriComponentsBuilder
          .fromCurrentRequest().path("/{id}")
          .buildAndExpand(cej.getCollectionExerciseId()).toUri().toString();

      return ResponseEntity.created(URI.create(newResourceUrl)).body(sampleUnitsRequest);

  }

  @RequestMapping(value = "/{type}/fileupload", method = RequestMethod.POST, consumes = "multipart/form-data")
  public final @ResponseBody ResponseEntity<SampleSummary> uploadSampleFile(@PathVariable("type") final String type, @RequestParam("file") MultipartFile file) throws CTPException {
    log.debug("Entering Sample file upload for Type {}", type);

    SampleSummary sampleSummary;
    try {
      sampleSummary = sampleService.ingest(file);
    } catch (Exception e) {
      throw new CTPException(CTPException.Fault.VALIDATION_FAILED, e, "Error ingesting file %s", file.getOriginalFilename());
    }

    return ResponseEntity.created(URI.create("TODO")).body(sampleSummary);
  }


  /**
   * GET endpoint for retrieving a list of all existing SampleSummaries
   *
   * @return a list of all existing SampleSummaries
   */
  @RequestMapping(value = "/samplesummaries", method = RequestMethod.GET)
  public ResponseEntity<List<SampleSummaryDTO>> findSampleSummaries() {
    List<SampleSummary> sampleSummaries = sampleService.findAllSampleSummaries();
    if (CollectionUtils.isEmpty(sampleSummaries)) {
      return ResponseEntity.noContent().build();
    } else {
      List<SampleSummaryDTO> result = mapperFacade.mapAsList(sampleSummaries, SampleSummaryDTO.class);
      return ResponseEntity.ok(result);
    }

  }

  /**
   * GET endpoint to return the SampleSummary info for the given sampleSummaryId
   *
   * @param sampleSummaryId the id fo the SampleSummary to search for
   * @return SampleSummaryDTO for the requested sampleSummaryId
   * @throws CTPException if SampleSummary not found
   */
  @RequestMapping(value = "/samplesummary/{sampleSummaryId}", method = RequestMethod.GET)
  public ResponseEntity<SampleSummaryDTO> findSampleSummary(@PathVariable("sampleSummaryId") final UUID sampleSummaryId) throws CTPException {
    SampleSummary sampleSummary = sampleService.findSampleSummary(sampleSummaryId);
    if (sampleSummary == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Sample Summary not found for sampleSummaryId %s", sampleSummaryId));
    }
    SampleSummaryDTO result = mapperFacade.map(sampleSummary, SampleSummaryDTO.class);

    return ResponseEntity.ok(result);

  }
}
