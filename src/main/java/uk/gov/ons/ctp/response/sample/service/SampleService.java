package uk.gov.ons.ctp.response.sample.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import validation.SampleUnitBaseVerify;
import validation.SurveyBaseVerify;

import java.util.List;

/**
 * The SampleService interface defines all business behaviours for operations on the Sample entity model.
 */
public interface SampleService {

  /**
   * Create and save a SampleSummary from the incoming SurveySample
   *
   * @param samplingUnitList list of sampling units.
   * @param surveySampleObject SurveySample to be used
   * @throws Exception exception thrown
   */
  SampleSummary processSampleSummary(SurveyBaseVerify surveySampleObject, List<? extends SampleUnitBaseVerify> samplingUnitList)
          throws Exception;

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
   * @throws Exception exception thrown
   */
  void sendToPartyService(PartyCreationRequestDTO party) throws Exception;

  /**
   * Ingest Survey Sample
   *
   * @param file Multipart File of SurveySample to be used
   * @throws Exception exception thrown
   */
  SampleSummary ingest(MultipartFile file) throws Exception;


}
