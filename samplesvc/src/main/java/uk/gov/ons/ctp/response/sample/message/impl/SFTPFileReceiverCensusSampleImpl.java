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
   * Receives invalid XML for CensusSurveySample
   * @param message invalid XML message
   * @throws CTPException if update operation fails
   */
  @ServiceActivator(inputChannel = "xmlInvalidCensus")
  public void invalidXMLProcess(Message<String> message) throws CTPException {
    log.info("xmlInvalidCensus: " + message.getHeaders().get("file_name"));
  }

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

}
