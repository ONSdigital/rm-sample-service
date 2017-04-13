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
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.SampleReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all census logic operating
 * on the Sample entity model for Census samples.
 */
@Slf4j
@MessageEndpoint
public class CensusSampleReceiverImpl implements SampleReceiver<CensusSurveySample> {

  @Inject
  private SampleService sampleService;

  /**
   * Processes CensusSurveySample transformed from XML
   * @param censusSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedCensus")
  public void processSample(CensusSurveySample censusSurveySample) {
    log.debug("CensusSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        censusSurveySample.getCollectionExerciseRef());

    SampleSummary savedSampleSummary = sampleService.processSampleSummary(censusSurveySample);
    List<CensusSampleUnit> samplingUnitList = censusSurveySample.getSampleUnits().getCensusSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sampleService.sendCensusToParty(savedSampleSummary.getSampleId(), samplingUnitList);

  }

}
