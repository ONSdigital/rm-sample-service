package uk.gov.ons.ctp.response.sample.message.impl;

import javax.xml.bind.JAXBElement;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;

@Slf4j
@MessageEndpoint
public class SFTPFileReceiverImpl {

/*  @Inject
  private ReceiptService receiptService;

  @Inject
  private DistributedLockManager sdxLockManager;*/

//  @ServiceActivator(inputChannel = "sampleXmlValid")
//  public void processFile(Message<String> message) throws CTPException {
//    //String filename = (String) message.getHeaders().get("file_remoteFile");
//    
//    String file = message.getPayload();
//    
//    //TODO Spring integration XML validation.
//    
//    System.out.println("File: " + file);
//    
//    for (String key: message.getHeaders().keySet()) {
//      
//      System.out.println(key + " : " + message.getHeaders().get(key));
//      
//    }
    
/*    if (!sdxLockManager.isLocked(filename) && sdxLockManager.lock(filename)) {
      log.debug(filename + ": is not locked");
      receiptService.acknowledgeFile(message.getPayload());
      log.debug(filename + ": grabbed locked");
      try {
        Thread.sleep(4000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      log.debug(filename + ": was already locked");
      Closeable closeable = new IntegrationMessageHeaderAccessor(message).getCloseableResource();
      if (closeable != null) {
        try {
          closeable.close();
        } catch (IOException e) {
          log.error("IOException thrown while closing Message Stream...", e.getMessage());
          throw new CTPException(CTPException.Fault.SYSTEM_ERROR, e.getMessage());
        }
      }

      message = null;
    }*/
    //System.out.println(filename);
//    Closeable closeable = new IntegrationMessageHeaderAccessor(message).getCloseableResource();
//    if (closeable != null) {
//      try {
//        closeable.close();
//        System.out.println("Resource Closed Successfully");
//      } catch (IOException e) {
//        log.error("IOException thrown while closing Message Stream...", e.getMessage());
//        throw new CTPException(CTPException.Fault.SYSTEM_ERROR, e.getMessage());
//      }
//    } else {
//      System.out.println("Closeable method is null");
//    }
//  }

  @ServiceActivator(inputChannel = "invalidSample")
  public void invalidFile(Message<String> message) throws CTPException {
    //String filename = (String) message.getHeaders().get("file_remoteFi
    System.out.println("INVALID");
  }
  
  /**
   * To process CaseReceipts read from queue
   * @param caseReceipt to process
   */
  @ServiceActivator(inputChannel = "sampleXmlTransformed")
  public void process(JAXBElement caseReceipt) {
    log.debug("entering process with caseReceipt {}", caseReceipt);
    /*System.out.println("Respondent: " + caseReceipt.getName());
    System.out.println("Respondent Content Model: " + caseReceipt.getValue());*/
    System.out.println("Respondent All: " + caseReceipt.toString());
  }
  
  /**
   * Using JPA entities to update repository for actionIds exported was slow.
   * JPQL queries used for performance reasons. To increase performance updates
   * batched with IN clause.
   *
   * @param message
   *          Spring integration message sent
   */
/*  @Override
  @ServiceActivator(inputChannel = "sftpSuccessProcess")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_remoteFile");
    log.debug("sftpSuccessProcess: " + filename);
    sdxLockManager.unlock(filename);
  }

  @Override
  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_remoteFile");
    log.debug("sftpFailedProcess: " + filename);
    sdxLockManager.unlock(filename);
  }*/

}
