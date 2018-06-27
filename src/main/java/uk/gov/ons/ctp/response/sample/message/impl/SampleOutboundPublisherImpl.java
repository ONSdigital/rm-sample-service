package uk.gov.ons.ctp.response.sample.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.SampleOutboundPublisher;

/** Publisher of messages about samples uploaded */
@Slf4j
@MessageEndpoint
public class SampleOutboundPublisherImpl implements SampleOutboundPublisher {

  private static final String UPLOAD_STARTED_ROUTING_KEY = "Sample.SampleUploadStarted.binding";
  private static final String UPLOAD_FINISHED_ROUTING_KEY = "Sample.SampleUploadFinished.binding";

  @Qualifier("sampleOutboundRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void sampleUploadStarted(SampleSummary sampleSummary) throws CTPException {
    sendMessage(UPLOAD_STARTED_ROUTING_KEY, sampleSummary);
  }

  @Override
  public void sampleUploadFinished(SampleSummary sampleSummary) throws CTPException {
    sendMessage(UPLOAD_FINISHED_ROUTING_KEY, sampleSummary);
  }

  private void sendMessage(String routingKey, SampleSummary sampleSummary) throws CTPException {
    try {
      String message = this.objectMapper.writeValueAsString(sampleSummary);
      log.debug("Sending message to routing key {} - {}", routingKey, message);
      rabbitTemplate.convertAndSend(routingKey, message);
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          String.format("Failed to create JSON message from sample summary - %v", sampleSummary),
          e);
    }
  }
}
