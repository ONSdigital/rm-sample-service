package uk.gov.ons.ctp.response.sample.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

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
  void processSampleSummary(SurveyBase surveySampleObject, List<? extends SampleUnitBase> samplingUnitList)
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
  void sendToPartyService(Party party) throws Exception;
}
