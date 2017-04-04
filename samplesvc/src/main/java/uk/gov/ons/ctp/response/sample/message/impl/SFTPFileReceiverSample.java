package uk.gov.ons.ctp.response.sample.message.impl;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;

@MessageEndpoint
public interface SFTPFileReceiverSample<T extends SurveyBase> {

  public void invalidXMLProcess(Message<String> message) throws CTPException;
  
  public void transformedXMLProcess(T surveySampleObject);

  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message);

  public void sftpFailedProcess(GenericMessage<MessagingException> message);

}
