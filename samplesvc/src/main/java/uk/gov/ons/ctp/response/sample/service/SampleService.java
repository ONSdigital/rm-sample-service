package uk.gov.ons.ctp.response.sample.service;

import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

public interface SampleService {
  
  /**
   * Create a SampleSummary from the incoming SurveySample
   *
   * @param sampleSummary SampleSummary to be created
   * @return the created SampleSummary
   */
  SampleSummary createSampleSummary(SampleSummary sampleSummary);
  
  /**
   * Create a SampleUnit from the incoming SurveySample
   *
   * @param sampleUnit SampleUnit to be created
   * @return the created SampleUnit
   */
  SampleUnit createSampleUnit(SampleUnit sampleUnit);
  
  /**
   * Find SampleSummary entity by sampleid
   *
   * @param sampleId The sampleId 
   * @return SampleSummary object or null
   */
  SampleSummary findSampleSummaryBySampleId(Integer sampleId);
  
}
