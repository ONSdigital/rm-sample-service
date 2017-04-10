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
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all business logic operating
 * on the Sample entity model for Social samples.
 */
@Slf4j
@MessageEndpoint
public class SFTPFileReceiverSocialSampleImpl implements SFTPFileReceiverSample<SocialSurveySample> {

  @Inject
  private SampleService sampleService;

  /**
   * Receives invalid XML for SocialSurveySample
   * @param message invalid XML message
   * @throws CTPException if update operation fails
   */
  @ServiceActivator(inputChannel = "xmlInvalidSocial")
  public void invalidXMLProcess(Message<String> message) throws CTPException {
    log.info("xmlInvalidSocial: " + message.getHeaders().get("file_name"));
  }

  /**
   * To process SocialSurveySample transformed from XML
   * @param socialSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedSocial")
  public void transformedXMLProcess(SocialSurveySample socialSurveySample) {
    log.info(String.format("SocialSurveySample (Collection Exercise Ref: %s) transformed successfully.",
        socialSurveySample.getCollectionExerciseRef()));

    SampleSummary savedSampleSummary = sampleService.createandSaveSampleSummary(socialSurveySample);

    List<SocialSampleUnit> samplingUnitList = socialSurveySample.getSampleUnits().getSocialSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);

  }

  /**
   * Confirms file rename successful for XML input file
   * @param message success message
   */
  @ServiceActivator(inputChannel = "renameSuccessProcessSocial")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get("file_name");
    log.info("Renaming successful for " + filename);
  }

  /**
   * Confirms file rename unsuccessful for XML input file
   * @param message failure message
   */
  @ServiceActivator(inputChannel = "renameFailedProcessSocial")
  public void sftpFailedProcess(GenericMessage<MessagingException> message) {
    String filename = (String) message.getPayload().getFailedMessage().getHeaders().get("file_name");
    log.info("Renaming failed for" + filename);
  }

}
