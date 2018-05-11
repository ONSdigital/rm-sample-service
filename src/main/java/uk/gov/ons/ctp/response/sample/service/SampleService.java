package uk.gov.ons.ctp.response.sample.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import validation.SampleUnitBase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

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
   *
   * @param sampleSummary the sample summary being processed
   * @param samplingUnitList list of sampling units.
   */
  SampleSummary processSampleSummary(SampleSummary sampleSummary, List<? extends SampleUnitBase> samplingUnitList);

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

    Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, String message);

  Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, Exception exception);

  /**
   * Ingest Survey Sample
   *
   * @param file Multipart File of SurveySample to be used
   * @param type Type of Survey to be used
   * @throws Exception exception thrown
   */
  Pair<SampleSummary, Future<Optional<SampleSummary>>> ingest(MultipartFile file, String type) throws Exception;

  /**
   * find sampleUnit
   *
   * @return SampleUnit
   */
  SampleUnit findSampleUnit(UUID id);
}
