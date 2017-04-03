package uk.gov.ons.ctp.response.sample.message.impl;

import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;

@Slf4j
@MessageEndpoint
public class SFTPFileReceiverBusinessSampleImpl {

  @ServiceActivator(inputChannel = "sampleXmlInvalid")
  public void invalidFile(Message<String> message) throws CTPException {
    log.info("sampleXmlInvalid: " + message.getHeaders().get("file_name"));
  }
  
  /**
   * To process CaseReceipts read from queue
   * @param businessSurveySample to process
   */
  @ServiceActivator(inputChannel = "sampleXmlTransformed", outputChannel = "")
  public void process(BusinessSurveySample businessSurveySample) {
    log.info("BusinessSurveySample Transformed Successfully " + businessSurveySample.getCollectionExerciseRef());
  }
  
  /**
   * Using JPA entities to update repository for actionIds exported was slow.
   * JPQL queries used for performance reasons. To increase performance updates
   * batched with IN clause.
   *
   * @param message Spring integration message sent
   * @throws DatatypeConfigurationException 
   */

  @ServiceActivator(inputChannel = "sftpSuccessProcess")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_name");
    log.info("sftpSuccessProcess: " + filename);
  }

  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
    log.info("sftpFailedProcess: " + filename);
  }

}
