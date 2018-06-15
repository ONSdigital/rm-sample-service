package uk.gov.ons.ctp.response.sample.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import validation.SampleUnitBase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The SampleService interface defines all business behaviours for operations on the Sample entity model.
 */
public interface SampleService {

  /**
   * find all sampleSummaries
   *
   * @return list of SampleSummary
   */
  List<SampleSummary> findAllSampleSummaries();

  /**
   * find sampleSummary
   *
   * @return SampleSummary
   */
  SampleSummary findSampleSummary(UUID id);

  /**
   * Create and save a SampleSummary from the incoming SurveySample
   *  @param sampleSummary the sample summary being processed
   * @param samplingUnitList list of sampling units.
   * @param sampleUnitState
   */
  SampleSummary saveSample(SampleSummary sampleSummary, List<? extends SampleUnitBase> samplingUnitList, SampleUnitState sampleUnitState);

  /**
   * Create a new sample summary and persist to the database
   * @return the newly created sample summary
   */
  SampleSummary createAndSaveSampleSummary();

  /**
   * Update the SampleSummary state
   *
   * @param sampleSummaryPK The sampleSummaryPK
   * @return SampleSummary object or null
   * @throws CTPException if transition errors
   */
  SampleSummary activateSampleSummaryState(Integer sampleSummaryPK) throws CTPException;


  /**
   * Save a CollectionExerciseJob based on the associated CollectionExerciseId, and SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param collectionExerciseJob CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Integer sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  Integer initialiseCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob) throws CTPException;

  /**
   * Post to partySvc.
   * If successful, it then goes on to change SampleUnit(s) state and to effect a state transition for the target
   * SampleSummary if one is required.
   *
   * @param party party picked up from queue
   * @return the Party representation data
   * @throws Exception exception thrown
   */
  PartyDTO sendToPartyService(PartyCreationRequestDTO party) throws Exception;

  /**
   * Ingest survey sample
   * @param sampleSummary a newly created samplesummary
   * @param file Multipart File of SurveySample to be used
   * @param type Type of Survey to be used
   * @return an updated samplesummary
   * @throws Exception thrown if issue reading CSV
   */
  SampleSummary ingest(SampleSummary sampleSummary, MultipartFile file, String type) throws Exception;

  Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, SampleSummaryDTO.ErrorCode errorCode,
                                            String message);

  Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, SampleSummaryDTO.ErrorCode errorCode,
                                            Exception exception);

  /**
   * find sampleUnit
   *
   * @return SampleUnit
   */
  SampleUnit findSampleUnit(UUID id);
}
