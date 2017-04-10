package uk.gov.ons.ctp.response.sample.service;

import java.sql.Timestamp;
import java.util.List;

import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
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
   * @param <T> The expected class of the value.
   * @param surveySampleObject SurveySample to be used
   * @return the created SampleSummary
   */
  <T extends SurveyBase> SampleSummary createandSaveSampleSummary(T surveySampleObject);

  /**
   * Create and save SampleUnits from the incoming SurveySample
   *
   * @param <T> The expected class of the value.
   * @param samplingUnitList List of SampleUnits to save
   * @param sampleSummary SampleSummary to be used
   */
  <T extends SampleUnitBase> void createandSaveSampleUnits(List<T> samplingUnitList, SampleSummary sampleSummary);

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
   * Sends relevant Census information to the Party service
   *
   * @param sampleId The sampleId
   * @param censusSurveySample object or null
   */
  void sendCensusToParty(Integer sampleId, CensusSurveySample censusSurveySample);

  /**
   * Sends relevant Social information to the Party service
   *
   * @param sampleId The sampleId
   * @param socialSurveySample object or null
   */
  void sendSocialToParty(Integer sampleId, SocialSurveySample socialSurveySample);

  /**
   * Sends relevant Business information to the Party service
   *
   * @param sampleId The sampleId
   * @param businessSurveySample object or null
   */
  void sendBusinessToParty(Integer sampleId, BusinessSurveySample businessSurveySample);

  /**
   * Update the SampleSummary state
   *
   * @param surveyRef The surveyRef
   * @param exerciseDateTime The effectiveStartDateTime
   * @return List<SampleUnit> object list or null
   */
  List<SampleUnit> findSampleUnitsBySurveyRefandExerciseDateTime(String surveyRef, Timestamp exerciseDateTime);
}
