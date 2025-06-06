package uk.gov.ons.ctp.response.sample.endpoint;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static uk.gov.ons.ctp.response.sample.mapper.CustomObjectMapper.*;

import com.opencsv.bean.CsvToBean;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import libs.common.error.CTPException;
import libs.common.error.InvalidRequestException;
import libs.sample.validation.BusinessSampleUnit;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.client.CollectionExerciseSvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.*;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleUnitException;

/** The REST endpoint controller for Sample Service. */
@RestController
@RequestMapping(value = "/samples", produces = "application/json")
public final class SampleEndpoint extends CsvToBean<BusinessSampleUnit> {
  private static final Logger log = LoggerFactory.getLogger(SampleEndpoint.class);

  private static final int NUM_UPLOAD_THREADS = 5;
  private static final ExecutorService EXECUTOR_SERVICE =
      Executors.newFixedThreadPool(NUM_UPLOAD_THREADS);
  private SampleService sampleService;

  @Autowired
  public SampleEndpoint(SampleService sampleService) {
    this.sampleService = sampleService;
  }

  @Autowired private CollectionExerciseSvcClient collectionExerciseSvcClient;

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
      List<SampleSummaryDTO> result = mapSampleSummariesToListOfDTO(sampleSummaries);
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
  public ResponseEntity<SampleSummaryDTO> findSampleSummary(
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId) throws CTPException {
    SampleSummary sampleSummary = sampleService.findSampleSummary(sampleSummaryId);
    if (sampleSummary == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Sample Summary not found for sampleSummaryId %s", sampleSummaryId));
    }
    SampleSummaryDTO result = mapSampleSummaryDTO(sampleSummary);

    return ResponseEntity.ok(result);
  }

  /**
   * DELETE endpoint to delete the sample summary and all related sample units given sampleSummaryId
   *
   * @param sampleSummaryId the id fo the SampleSummary to delete
   * @return noContent on success
   * @throws CTPException if SampleSummary not found
   */
  @RequestMapping(value = "/samplesummary/{sampleSummaryId}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteSampleSummary(
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId) throws CTPException {
    SampleSummary sampleSummary = sampleService.findSampleSummary(sampleSummaryId);
    log.info(
        "Attempting to delete sample units and sample summary records",
        kv("sampleSummaryId", sampleSummaryId));
    if (sampleSummary == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Sample Summary not found for sampleSummaryId %s", sampleSummaryId));
    }
    sampleService.deleteSampleSummaryAndSampleUnits(sampleSummary);
    log.info(
        "Successfully deleted sample units and sample summary records",
        kv("sampleSummaryId", sampleSummaryId));

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/count", method = RequestMethod.GET)
  public ResponseEntity<SampleUnitsRequestDTO> getSampleSummaryUnitCount(
      @RequestParam(value = "sampleSummaryId") List<UUID> sampleSummaryIdList) {

    int sampleUnitsTotal = 0;

    for (UUID sampleSummaryId : sampleSummaryIdList) {
      sampleUnitsTotal += sampleService.getSampleSummaryUnitCount(sampleSummaryId);
    }

    SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO(sampleUnitsTotal);

    return ResponseEntity.ok(sampleUnitsRequest);
  }

  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public ResponseEntity<SampleUnitDTO> requestSampleUnit(
      @PathVariable("id") final UUID sampleUnitId) throws CTPException {
    SampleUnit sampleUnit = sampleService.findSampleUnit(sampleUnitId);
    if (sampleUnit == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("Sample Unit not found for sampleUnitId %s", sampleUnitId));
    }
    SampleUnitDTO result = mapSampleUnitDTO(sampleUnit);

    return ResponseEntity.ok(result);
  }

  @RequestMapping(value = "{sampleSummaryId}/sampleunits", method = RequestMethod.GET)
  public ResponseEntity<SampleUnitDTO[]> requestSampleUnitsForSampleSummary(
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId,
      @RequestParam(required = false) String state)
      throws CTPException {

    List<SampleUnit> sampleUnits;
    if (Strings.isEmpty(state)) {
      sampleUnits = sampleService.findSampleUnitsBySampleSummaryAsList(sampleSummaryId);
      List<SampleUnitDTO> result = mapSampleUnitsToListOfDTO(sampleUnits);

      if (!sampleUnits.isEmpty()) {
        return ResponseEntity.ok(result.toArray(new SampleUnitDTO[] {}));
      }

      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          String.format("No sample units were found for sample summary %s", sampleSummaryId));
    } else {
      log.debug(
          "finding samples with state", kv("sampleSummaryId", sampleSummaryId), kv("state", state));
      try {
        SampleUnitDTO.SampleUnitState sampleUnitState =
            SampleUnitDTO.SampleUnitState.valueOf(state);
        sampleUnits =
            sampleService.findSampleUnitsBySampleSummaryAndStateAsList(
                sampleSummaryId, sampleUnitState);
        log.info(
            "found samples with state",
            kv("sampleSummaryId", sampleSummaryId),
            kv("state", state),
            kv("numberOfSamples", sampleUnits.size()));

        List<SampleUnitDTO> result = mapSampleUnitsToListOfDTO(sampleUnits);

        return ResponseEntity.ok(result.toArray(new SampleUnitDTO[] {}));

      } catch (IllegalArgumentException | NullPointerException e) {
        log.error(
            "failed to find samples with state",
            kv("sampleSummaryId", sampleSummaryId),
            kv("state", state),
            e);
        throw new CTPException(
            CTPException.Fault.BAD_REQUEST, String.format("%s is not a valid state", state));
      }
    }
  }

  @RequestMapping(
      value = "{sampleSummaryId}/sampleunits/{sampleunitref}",
      method = RequestMethod.GET)
  public ResponseEntity<SampleUnitDTO> requestSampleUnitForSampleSummaryAndRuRef(
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId,
      @PathVariable("sampleunitref") final String sampleUnitRef) {
    try {
      log.debug(
          "attempting to find sample unit",
          kv("sampleSummaryId", sampleSummaryId),
          kv("sampleUnitRef", sampleUnitRef));
      SampleUnit sampleUnit =
          sampleService.findSampleUnitBySampleSummaryAndSampleUnitRef(
              sampleSummaryId, sampleUnitRef);
      SampleUnitDTO result = mapSampleUnitDTO(sampleUnit);
      log.debug(
          "found sample unit",
          kv("sampleSummaryId", sampleSummaryId),
          kv("sampleUnitRef", sampleUnitRef),
          kv("sampleUnitId", sampleUnit.getId()));
      return ResponseEntity.ok(result);
    } catch (UnknownSampleUnitException e) {
      log.warn(
          "unknown sample unit",
          kv("sampleSummaryId", sampleSummaryId),
          kv("sampleUnitRef", sampleUnitRef),
          e);
      return ResponseEntity.badRequest().build();
    } catch (UnknownSampleSummaryException e) {
      log.error("unknown sample summary id", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.badRequest().build();
    }
  }

  @RequestMapping(value = "{sampleSummaryId}/sampleunits/", method = RequestMethod.POST)
  public ResponseEntity<SampleUnitDTO> createSampleUnitsForSampleSummary(
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId,
      final @Valid @RequestBody BusinessSampleUnitDTO businessSampleUnitDTO,
      BindingResult bindingResult)
      throws InvalidRequestException {

    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException("Binding errors for create action: ", bindingResult);
    }
    log.debug(
        "create sample unit request received", kv("businessSampleUnitDTO", businessSampleUnitDTO));
    BusinessSampleUnit businessSampleUnit = mapBusinessUnit(businessSampleUnitDTO);

    log.debug("business sample constructed", kv("businessSample", businessSampleUnit));
    try {
      // first create the new sample
      SampleUnit sampleUnit =
          sampleService.createSampleUnit(
              sampleSummaryId, businessSampleUnit, SampleUnitDTO.SampleUnitState.INIT);
      log.debug("sample created");
      SampleUnitDTO sampleUnitDTO = mapSampleUnitDTO(sampleUnit);
      log.debug("created SampleUnitDTO", kv("sampleUnitDTO", sampleUnitDTO));
      return ResponseEntity.created(
              URI.create(String.format("/samples/%s", sampleUnit.getSampleUnitPK())))
          .contentType(MediaType.APPLICATION_JSON)
          .body(sampleUnitDTO);
    } catch (IllegalStateException e) {
      log.warn("duplicate sample", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (UnknownSampleSummaryException e) {
      log.error("unknown sample summary id", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.badRequest().build();
    } catch (CTPException | RuntimeException e) {
      log.error("unexpected exception", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @RequestMapping(
      value = "/samplesummary/{sampleSummaryId}/check-and-transition-sample-summary-status",
      method = RequestMethod.GET)
  public ResponseEntity<SampleSummaryLoadingStatus> checkAllSampleUnitsForSampleSummary(
      @PathVariable("sampleSummaryId") final UUID sampleSummaryId) {
    try {
      SampleSummaryLoadingStatus sampleSummaryLoadingStatus =
          sampleService.sampleSummaryStateCheck(sampleSummaryId);
      collectionExerciseSvcClient.collectionExerciseSampleSummaryReadiness(sampleSummaryId);
      return ResponseEntity.ok(sampleSummaryLoadingStatus);
    } catch (CTPException e) {
      log.error("unexpected exception", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NoSuchElementException e) {
      log.error("Sample summary not found", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @RequestMapping(value = "/samplesummary", method = RequestMethod.POST)
  public ResponseEntity<SampleSummaryDTO> createSampleSummary(
      final @RequestBody SampleSummaryDTO requestSummary) {
    SampleSummary sampleSummary = sampleService.createAndSaveSampleSummary(requestSummary);
    if (sampleSummary != null) {
      log.debug("sample summary created", kv("sampleSummaryId", sampleSummary.getId()));
    }
    // sampleSummary could be null do we want to handle this better?
    SampleSummaryDTO sampleSummaryDTO = mapSampleSummaryDTO(sampleSummary);

    return ResponseEntity.created(
            URI.create(String.format("/samplesummary/%s", sampleSummary.getId())))
        .contentType(MediaType.APPLICATION_JSON)
        .body(sampleSummaryDTO);
  }
}
