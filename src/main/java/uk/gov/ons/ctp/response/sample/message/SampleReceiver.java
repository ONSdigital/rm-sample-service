package uk.gov.ons.ctp.response.sample.message;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import validation.SurveyBaseVerify;

import java.util.Map;

/**
* The SampleService interface defines all business behaviours for operations on
* the Sample entity model.
* @param <T> Survey Sample object extended from SurveyBase
*/
public interface SampleReceiver<T extends SurveyBaseVerify> {

  /**
   * Processes CensusSurveySample transformed from XML
   * @param surveySampleObject surveySample transformed from XML
   * @param headerMap Map of Header
   * @return Message<String> message containing sample payload
   * @throws Exception error exception thrown
   */
  Message<String> processSample(T surveySampleObject, @Headers Map<String, Object> headerMap) throws Exception;

}
