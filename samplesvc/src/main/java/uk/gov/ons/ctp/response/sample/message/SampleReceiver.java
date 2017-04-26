package uk.gov.ons.ctp.response.sample.message;

import uk.gov.ons.ctp.response.sample.definition.SurveyBase;

/**
* The SampleService interface defines all business behaviours for operations on
* the Sample entity model.
* @param <T> Survey Sample object extended from SurveyBase
*/
public interface SampleReceiver<T extends SurveyBase> {

  /**
   * Processes CensusSurveySample transformed from XML
   * @param surveySampleObject surveySample transformed from XML
   */
  void processSample(T surveySampleObject);

}
