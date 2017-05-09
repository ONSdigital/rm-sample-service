package uk.gov.ons.ctp.response.sample.message.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
import uk.gov.ons.ctp.response.sample.message.SampleReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all social logic operating
 * on the Sample entity model for Social samples.
 */
@Slf4j
@MessageEndpoint
public class SocialSampleReceiverImpl implements SampleReceiver<SocialSurveySample> {

  @Autowired
  private SampleService sampleService;

  /**
   * To process SocialSurveySample transformed from XML
   * @param socialSurveySample to process
   * @return
   */
  @ServiceActivator(inputChannel = "xmlTransformedSocial", outputChannel = "renameSocialXMLFile")
  public Message<String> processSample(SocialSurveySample socialSurveySample ,@Headers Map<String, Object> headerMap) throws Exception{
    log.debug("SocialSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        socialSurveySample.getCollectionExerciseRef());
    List<SocialSampleUnit> samplingUnitList = socialSurveySample.getSampleUnits().getSocialSampleUnits();
    sampleService.processSampleSummary(socialSurveySample, samplingUnitList);
   
    String load = "";
    String fileName = (String)headerMap.get("file_name");
    final Message<String> message = MessageBuilder.withPayload(load).setHeader("file_name",fileName).build();
    return message;
  }

}
