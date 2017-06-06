package uk.gov.ons.ctp.response.sample.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.support.MessageBuilder;
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.message.SampleReceiver;
import uk.gov.ons.ctp.response.sample.service.SampleService;

import java.util.List;
import java.util.Map;

/**
 * A SampleService implementation which encapsulates all census logic operating
 * on the Sample entity model for Census samples.
 */
@Slf4j
@MessageEndpoint
public class CensusSampleReceiverImpl implements SampleReceiver<CensusSurveySample> {

  @Autowired
  private SampleService sampleService;

  /**
   * Processes CensusSurveySample transformed from XML
   * @param censusSurveySample to process
   * @param headerMap map of header
   * @return Message<String> message containing sample payload
   * @throws Exception error exception thrown
   */
  @ServiceActivator(inputChannel = "xmlTransformedCensus", outputChannel = "renameCensusXMLFile")
  public Message<String> processSample(CensusSurveySample censusSurveySample,
                                       @Headers Map<String, Object> headerMap) throws Exception {
    log.debug("CensusSurveySample (Collection Exercise Ref: {}) transformed successfully.",
        censusSurveySample.getCollectionExerciseRef());
    List<CensusSampleUnit> samplingUnitList = censusSurveySample.getSampleUnits().getCensusSampleUnits();
    sampleService.processSampleSummary(censusSurveySample,  samplingUnitList);

    String load = "";
    String fileName = (String) headerMap.get("file_name");
    final Message<String> message = MessageBuilder.withPayload(load).setHeader(fileName, "file_name").build();
    return message;
  }

}
