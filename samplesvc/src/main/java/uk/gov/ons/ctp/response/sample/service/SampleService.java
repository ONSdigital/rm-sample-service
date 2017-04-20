package uk.gov.ons.ctp.response.sample.service;

import java.sql.Timestamp;
import java.util.List;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

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
   * Save a CollectionExerciseJob based on the associated CollectionExerciseId, and SampleSummary surveyRef and
   * exerciseDateTime
   *
   * @param collectionExerciseId collectionExerciseId to which SampleUnits are related
   * @param surveyRef surveyRef to which SampleUnits are related
   * @param exerciseDateTime exerciseDateTime to which SampleUnits are related
   * @return Integer sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  Integer initialiseCollectionExerciseJob(Integer collectionExerciseId, String surveyRef, Timestamp exerciseDateTime) 
      throws CTPException;
}
