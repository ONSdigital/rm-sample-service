package uk.gov.ons.ctp.response.sample.service;

import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

public interface BusinessSampleService {
  
  /**
   * Create a Sample from the incoming BusinessSurveySample
   *
   * @param businessSurveySampleSummary BusinessSurveySampleSummary to be created
   * @param timestamp timestamp equals to the incoming BusinessSurveySampleSummary's responseDateTime
   * @param state the state of the incoming BusinessSurveySampleSummary
   * @return the created BusinessSurveySampleSummary
   */
  SampleSummary createSampleSummary(SampleSummary sampleSummary);
  
  SampleUnit createSampleUnit(SampleUnit sampleUnit);
  
  /**
   * Find SampleSummary entity by sampleid
   *
   * @param sampleId The sampleId 
   * @return SampleSummary object or null
   */
  SampleSummary findSampleSummaryBySampleId(Integer sampleId);
  
}
