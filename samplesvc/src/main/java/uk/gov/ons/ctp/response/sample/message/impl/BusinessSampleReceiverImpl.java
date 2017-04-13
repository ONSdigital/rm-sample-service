package uk.gov.ons.ctp.response.sample.message.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.SampleReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * A SampleService implementation which encapsulates all business logic operating
 * on the Sample entity model for Business samples.
 */
@Slf4j
@MessageEndpoint
public class BusinessSampleReceiverImpl implements SampleReceiver<BusinessSurveySample> {

  @Inject
  private SampleService sampleService;

  /**
   * To process BusinessSurveySample transformed from XML
   * @param businessSurveySample to process
   */
  @ServiceActivator(inputChannel = "xmlTransformedBusiness")
  public void processSample(BusinessSurveySample businessSurveySample) {
    log.debug("BusinessSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        businessSurveySample.getCollectionExerciseRef());

    SampleSummary savedSampleSummary = sampleService.processSampleSummary(businessSurveySample);
    List<BusinessSampleUnit> samplingUnitList = businessSurveySample.getSampleUnits().getBusinessSampleUnits();
    sampleService.createandSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sampleService.sendBusinessToParty(savedSampleSummary.getSampleId(), samplingUnitList);
  }

}
