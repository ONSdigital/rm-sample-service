package uk.gov.ons.ctp.response.sample.message.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@Slf4j
@MessageEndpoint
public class SFTPFileReceiverSocialSampleImpl implements SFTPFileReceiverSample<SocialSurveySample> {

  @Inject
  private SampleService sampleService;
  
  @ServiceActivator(inputChannel = "xmlInvalidSocial")
  public void invalidXMLProcess(Message<String> message) throws CTPException {
    log.info("xmlInvalidSocial: " + message.getHeaders().get("file_name"));
  }
  
  /**
   * To process SocialSurveySample transformed from XML
   * @param SocialSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedSocial")
  public void transformedXMLProcess(SocialSurveySample socialSurveySample) {
    log.info(String.format("SocialSurveySample (Collection Exercise Ref: %s) transformed successfully.", socialSurveySample.getCollectionExerciseRef()));

    Timestamp effectiveStartDateTime = new Timestamp(socialSurveySample.getEffectiveStartDateTime().toGregorianCalendar().getTimeInMillis());
    Timestamp effectiveEndDateTime = new Timestamp(socialSurveySample.getEffectiveEndDateTime().toGregorianCalendar().getTimeInMillis());
    
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setEffectiveStartDateTime(effectiveStartDateTime);
    sampleSummary.setEffectiveEndDateTime(effectiveEndDateTime);
    sampleSummary.setSurveyRef(socialSurveySample.getSurveyRef());
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);
    
    SampleSummary savedSampleSummary = sampleService.createSampleSummary(sampleSummary);
    
    List<SocialSampleUnit> samplingUnitList = socialSurveySample.getSampleUnits().getSocialSampleUnits();
    
    for (SocialSampleUnit socialSampleUnit : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleId(savedSampleSummary.getSampleId());
      sampleUnit.setSampleUnitRef(socialSampleUnit.getSampleUnitRef());
      sampleUnit.setSampleUnitType(socialSampleUnit.getSampleUnitType());
      
      sampleService.createSampleUnit(sampleUnit);
      
    }
    
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
