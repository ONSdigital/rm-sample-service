package uk.gov.ons.ctp.response.sample.message.impl;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.SampleReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all social logic operating
 * on the Sample entity model for Social samples.
 */
@Slf4j
@MessageEndpoint
public class SocialSampleReceiverImpl implements SampleReceiver<SocialSurveySample> {

  @Inject
  private SampleService sampleService;

  /**
   * To process SocialSurveySample transformed from XML
   * @param socialSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedSocial")
  public void processSample(SocialSurveySample socialSurveySample) {
    log.debug("SocialSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        socialSurveySample.getCollectionExerciseRef());

    SampleSummary savedSampleSummary = sampleService.processSampleSummary(socialSurveySample);
    List<SocialSampleUnit> samplingUnitList = socialSurveySample.getSampleUnits().getSocialSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sampleService.sendSocialToParty(savedSampleSummary.getSampleId(), samplingUnitList);

  }

}
