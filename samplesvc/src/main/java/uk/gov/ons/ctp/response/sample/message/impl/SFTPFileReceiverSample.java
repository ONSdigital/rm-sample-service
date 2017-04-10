package uk.gov.ons.ctp.response.sample.message.impl;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;

/**
* The SampleService interface defines all business behaviours for operations on
* the Sample entity model.
* @param <T> Survey Sample object extended from SurveyBase
*/
@MessageEndpoint
public interface SFTPFileReceiverSample<T extends SurveyBase> {

  /**
   * Receives invalid XML for CensusSurveySample
   * @param message invalid XML message
   * @throws CTPException if update operation fails
   *
   */
  void invalidXMLProcess(Message<String> message) throws CTPException;

  /**
   * Processes CensusSurveySample transformed from XML
   * @param surveySampleObject surveySample transformed from XML
   */
  void transformedXMLProcess(T surveySampleObject);

  /**
   * Confirms file rename successful for XML input file
   * @param message success message
   */
  void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message);

  /**
   * Confirms file rename unsuccessful for XML input file
   * @param message failure message
   */
  void sftpFailedProcess(GenericMessage<MessagingException> message);

}
