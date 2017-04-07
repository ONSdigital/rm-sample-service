package uk.gov.ons.ctp.response.sample.service;

import java.util.List;

import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

public interface SampleService {
   
  /**
   * Create and save a SampleSummary from the incoming SurveySample
   *
   * @param surveySampleObject SurveySample to be used
   * @return the created SampleSummary
   */
  public <T extends SurveyBase> SampleSummary createandSaveSampleSummary(T surveySampleObject);

  /**
   * Create and save SampleUnits from the incoming SurveySample
   * 
   * @param samplingUnitList List of SampleUnits to save
   * @param sampleSummary SampleSummary to be used
   */
  public <T extends SampleUnitBase> void createandSaveSampleUnits(List<T> samplingUnitList, SampleSummary sampleSummary);
    
  /**
   * Find SampleSummary entity by sampleid
   *
   * @param sampleId The sampleId 
   * @return SampleSummary object or null
   */
  SampleSummary findSampleSummaryBySampleId(Integer sampleId);
 
  /**
   * update sample summary status
   *
   * @param sampleId The sampleId 
   * @return SampleSummary object or null
   */
  public void activateSampleSummaryState(Integer sampleId);

  void sendToParty(BusinessSurveySample businessSurveySample);
}
