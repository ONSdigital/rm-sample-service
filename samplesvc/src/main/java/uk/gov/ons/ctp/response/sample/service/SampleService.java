package uk.gov.ons.ctp.response.sample.service;

import java.sql.Timestamp;
import java.util.List;

import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

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
   * @param surveyRef The surveyRef
   * @param exerciseDateTime The effectiveStartDateTime
   * @return List<SampleUnit> object list or null
   */
  List<SampleUnit> findSampleUnits(String surveyRef, Timestamp exerciseDateTime);
  
  /**
   * Update the SampleSummary state
   *
   * @param surveyRef The surveyRef
   * @param exerciseDateTime The effectiveStartDateTime
   * @return List<SampleUnit> object list or null
   */
  Integer findSampleUnitsSize(String surveyRef, Timestamp exerciseDateTime);
}
