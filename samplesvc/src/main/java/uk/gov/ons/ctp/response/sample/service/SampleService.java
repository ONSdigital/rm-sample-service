package uk.gov.ons.ctp.response.sample.service;

import java.util.List;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;

/**
 * The SampleService interface defines all business behaviours for operations on
 * the Sample entity model.
 */
public interface SampleService {

  /**
   * Create and save a SampleSummary from the incoming SurveySample
   *
   * @param samplingUnitList list of sampling units.
   * @param surveySampleObject SurveySample to be used
   */
  void processSampleSummary(SurveyBase surveySampleObject, List<? extends SampleUnitBase> samplingUnitList);

  /**
   * Find SampleSummary entity by sampleid
   *
   * @param sampleId The sampleId
   * @return SampleSummary object or null
   */
  SampleSummary findSampleSummaryBySampleId(Integer sampleId);

  /**
   * Update the SampleSummary state
   *
   * @param sampleId The sampleId
   * @return SampleSummary object or null
   */
  SampleSummary activateSampleSummaryState(Integer sampleId);

  /**
   * Update the SampleSummary state
   *
   * @param sampleId The sampleId
   * @return SampleSummary object or null
   */
  SampleUnit activateSampleUnitState(Integer sampleId);


  /**
   * Save a CollectionExerciseJob based on the associated CollectionExerciseId, and SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param collectionExerciseJobCreationRequestDTO CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Integer sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  Integer initialiseCollectionExerciseJob(
      CollectionExerciseJobCreationRequestDTO collectionExerciseJobCreationRequestDTO)
      throws CTPException;
}
