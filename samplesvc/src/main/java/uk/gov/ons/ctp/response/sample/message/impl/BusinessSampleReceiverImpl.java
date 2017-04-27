package uk.gov.ons.ctp.response.sample.message.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
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
 * @throws Exception 
   */
  @ServiceActivator(inputChannel = "xmlTransformedBusiness", outputChannel = "xmlRenameFile")
  public Message<String> processSample(BusinessSurveySample businessSurveySample ,@Headers Map<String, Object> headerMap) throws Exception {
    log.debug("BusinessSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        businessSurveySample.getCollectionExerciseRef());

    String load = "";
    String fileName = (String)headerMap.get("file_name");
    String type =(String)headerMap.get("sample_type");
   
    List<BusinessSampleUnit> samplingUnitList = businessSurveySample.getSampleUnits().getBusinessSampleUnits();
    sampleService.processSampleSummary(businessSurveySample, samplingUnitList);
    log.debug("rename.processed");
    final Message<String> message = MessageBuilder.withPayload(load).setHeader("file_name", fileName).build();
    return message;
  }

}
