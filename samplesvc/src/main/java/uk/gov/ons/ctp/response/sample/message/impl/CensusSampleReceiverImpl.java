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
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
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
 * @return 
 * @throws Exception 
   */
  @ServiceActivator(inputChannel = "xmlTransformedCensus" , outputChannel = "renameCensusXMLFile")
  public Message<String> processSample(CensusSurveySample censusSurveySample,@Headers Map<String, Object> headerMap) throws Exception {
    log.debug("CensusSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        censusSurveySample.getCollectionExerciseRef());

    String load = "";
    String fileName = (String)headerMap.get("file_name"); 
    
    List<CensusSampleUnit> samplingUnitList = censusSurveySample.getSampleUnits().getCensusSampleUnits();
    sampleService.processSampleSummary(censusSurveySample,  samplingUnitList);
  
    final Message<String> message = MessageBuilder.withPayload(load).setHeader(fileName, "file_name").build();
    return message;
  }

}
