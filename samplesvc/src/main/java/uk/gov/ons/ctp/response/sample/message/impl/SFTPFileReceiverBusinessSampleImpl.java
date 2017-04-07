package uk.gov.ons.ctp.response.sample.message.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@Slf4j
@MessageEndpoint
public class SFTPFileReceiverBusinessSampleImpl implements SFTPFileReceiverSample<BusinessSurveySample> {

  @Inject
  private SampleService sampleService;
  
  @ServiceActivator(inputChannel = "xmlInvalidBusiness")
  public void invalidXMLProcess(Message<String> message) throws CTPException {
    log.info("xmlInvalidBusiness: " + message.getHeaders().get("file_name"));
  }
  
  /**
   * To process BusinessSurveySample transformed from XML
   * @param BusinessSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedBusiness")
  public void transformedXMLProcess(BusinessSurveySample businessSurveySample) {
    log.info(String.format("BusinessSurveySample (Collection Exercise Ref: %s) transformed successfully.", businessSurveySample.getCollectionExerciseRef()));

    SampleSummary savedSampleSummary = sampleService.createandSaveSampleSummary(businessSurveySample);
    
    List<BusinessSampleUnit> samplingUnitList = businessSurveySample.getSampleUnits().getBusinessSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);

  }

  @ServiceActivator(inputChannel = "renameSuccessProcessBusiness")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_name");
    log.info("Renaming successful for " + filename);
  }

  @ServiceActivator(inputChannel = "renameFailedProcessBusiness")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
    log.info("Renaming failed for" + filename);    
  }

}
