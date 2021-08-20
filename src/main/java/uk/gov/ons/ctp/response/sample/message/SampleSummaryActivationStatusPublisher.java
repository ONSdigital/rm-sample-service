package uk.gov.ons.ctp.response.sample.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.SampleSvcApplication;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryStatusDTO;

/** SampleUnitPublisher publishes message to PubSub */
@Service
public class SampleSummaryActivationStatusPublisher {
  private static final Logger LOG =
      LoggerFactory.getLogger(SampleSummaryActivationStatusPublisher.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SampleSvcApplication.PubsubOutboundGateway messagingGateway;

  public void sendSampleSummaryActivation(SampleSummaryStatusDTO sampleSummaryStatus) {
    try {
      String payload = objectMapper.writeValueAsString(sampleSummaryStatus);
      messagingGateway.sendToPubsub(payload);
    } catch (JsonProcessingException e) {
      LOG.error("Failed to serialise sample summary activation", e);
    }
  }
}
