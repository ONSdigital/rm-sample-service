package uk.gov.ons.ctp.response.sample.message.impl;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.springframework.integration.MessageRejectedException;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all business logic operating
 * on the Sample entity model for Business samples.
 */
@Slf4j
@MessageEndpoint
public class SFTPFileReceiverBusinessSampleImpl implements SFTPFileReceiverSample<BusinessSurveySample> {

  @Inject
  private SampleService sampleService;

  /**
   * Receives invalid XML for BusinessSurveySample
   * @param message invalid XML message
   * @throws CTPException if update operation fails
   */
  @ServiceActivator(inputChannel = "xmlInvalidBusiness")
  public void invalidXMLProcess(Message<String> message) throws CTPException {
    log.info("xmlInvalidBusiness: " + message.getHeaders().get("file_name"));
  }

  /**
   * To process BusinessSurveySample transformed from XML
   * @param businessSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedBusiness")
  public void transformedXMLProcess(BusinessSurveySample businessSurveySample) {
    log.info(String.format("BusinessSurveySample (Collection Exercise Ref: %s) transformed successfully.",
        businessSurveySample.getCollectionExerciseRef()));

    SampleSummary savedSampleSummary = sampleService.createandSaveSampleSummary(businessSurveySample);
    List<BusinessSampleUnit> samplingUnitList = businessSurveySample.getSampleUnits().getBusinessSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sampleService.sendBusinessToParty(savedSampleSummary.getSampleId(), samplingUnitList);
  }

  /**
   * Confirms file rename successful for XML input file
   * @param message success message
   */
  @ServiceActivator(inputChannel = "renameSuccessProcessBusiness")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_name");
    log.info("Renaming successful for " + filename);
  }

  /**
   * Confirms file rename unsuccessful for XML input file
   * @param message failure message
   */
  @ServiceActivator(inputChannel = "renameFailedProcessBusiness")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
    log.info("Renaming failed for" + filename);
  }

  @ServiceActivator(inputChannel = "pollerErrorChannel", outputChannel ="errorUploadChannel")
  public Message<String> invalidXMLProcessPoll(GenericMessage errorMessage) throws CTPException, IOException {

    String fileName = ((MessageRejectedException) errorMessage.getPayload()).getFailedMessage().getHeaders().get("file_name").toString();
    String error = ((Exception)errorMessage.getPayload()).getCause().toString();
    String shortFileName = fileName.replace(".xml", "");
    String errorFile = shortFileName + "_error.txt";

    log.info(fileName + " Was invalid and rejected.");

    final Message<String> message = MessageBuilder.withPayload(error).setHeader( "error_file_name",errorFile)
            .setHeader("file_name",fileName).setHeader("short_file_name", shortFileName).build();

    return message;
  }

}
