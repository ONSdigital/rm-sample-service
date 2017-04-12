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
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all census logic operating
 * on the Sample entity model for Census samples.
 */
@Slf4j
@MessageEndpoint
public class SFTPFileReceiverCensusSampleImpl implements SFTPFileReceiverSample<CensusSurveySample> {

  @Inject
  private SampleService sampleService;

  /**
   * Processes CensusSurveySample transformed from XML
   * @param censusSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedCensus")
  public void transformedXMLProcess(CensusSurveySample censusSurveySample) {
    log.info(String.format("CensusSurveySample (Collection Exercise Ref: %s) transformed successfully.",
        censusSurveySample.getCollectionExerciseRef()));

    SampleSummary savedSampleSummary = sampleService.createandSaveSampleSummary(censusSurveySample);
    List<CensusSampleUnit> samplingUnitList = censusSurveySample.getSampleUnits().getCensusSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sampleService.sendCensusToParty(savedSampleSummary.getSampleId(), samplingUnitList);

  }

  /**
   * Confirms file rename successful for XML input file
   * @param message success message
   */
  @ServiceActivator(inputChannel = "renameSuccessProcessCensus")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_name");
    log.info("Renaming successful for " + filename);
  }

  /**
   * Confirms file rename unsuccessful for XML input file
   * @param message failure message
   */
  @ServiceActivator(inputChannel = "renameFailedProcessCensus")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
    log.info("Renaming failed for" + filename);
  }

  /**
   * Creates error file containing the reason for XML validation failure
   * @param errorMessage failure message containing reason for failure
   * @return Message<String> message containing cut down error message and new file names
   */
  @ServiceActivator(inputChannel = "pollerErrorChannelCensus", outputChannel ="errorUploadChannelCensus")
  public Message<String> invalidXMLProcessPoll(GenericMessage errorMessage) throws CTPException, IOException {

    String fileName = ((MessagingException) errorMessage.getPayload()).getFailedMessage().getHeaders().get("file_name").toString();
    String error = ((Exception)errorMessage.getPayload()).getCause().toString();
    String shortFileName = fileName.replace(".xml", "");
    String errorFile = shortFileName + "_error.txt";

    log.info(fileName + " Was invalid and rejected.");

    final Message<String> message = MessageBuilder.withPayload(error).setHeader( "error_file_name",errorFile)
            .setHeader("file_name",fileName).setHeader("short_file_name", shortFileName).build();

    return message;
  }
}
