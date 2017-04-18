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

  /**
   * Creates error file containing the reason for XML validation failure
   * @param errorMessage failure message containing reason for failure
   * @return Message<String> message containing cut down error message and new file names
   *//*
  Message<String> processInvalidSample(GenericMessage errorMessage) throws CTPException, IOException;*/

}
