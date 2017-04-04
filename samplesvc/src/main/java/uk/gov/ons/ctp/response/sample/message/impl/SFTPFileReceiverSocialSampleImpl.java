package uk.gov.ons.ctp.response.sample.message.impl;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;

@Slf4j
@MessageEndpoint
public class SFTPFileReceiverSocialSampleImpl implements SFTPFileReceiverSample<SocialSurveySample> {

  @ServiceActivator(inputChannel = "xmlInvalidSocial")
  public void invalidXMLProcess(Message<String> message) throws CTPException {
    log.info("xmlInvalidSocial: " + message.getHeaders().get("file_name"));
  }
  
  /**
   * To process SocialSurveySample transformed from XML
   * @param SocialSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedSocial")
  public void transformedXMLProcess(SocialSurveySample SocialSurveySample) {
    log.info(String.format("SocialSurveySample (Collection Exercise Ref: %s) transformed successfully.", SocialSurveySample.getCollectionExerciseRef()));
  }

  @ServiceActivator(inputChannel = "renameSuccessProcessSocial")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_name");
    log.info("Renaming successful for " + filename);
  }

  @ServiceActivator(inputChannel = "renameFailedProcessSocial")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
    log.info("Renaming failed for" + filename);    
  }

}
