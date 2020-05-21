package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

/** Publisher of messages about samples uploaded */
@MessageEndpoint
public class SampleOutboundPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleOutboundPublisher.class);

  private static final String UPLOAD_STARTED_ROUTING_KEY = "Sample.SampleUploadStarted.binding";
  private static final String UPLOAD_FINISHED_ROUTING_KEY = "Sample.SampleUploadFinished.binding";

  @Qualifier("sampleOutboundRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired private ObjectMapper objectMapper;

  public void sampleUploadStarted(SampleSummary sampleSummary) throws CTPException {
    sendMessage(UPLOAD_STARTED_ROUTING_KEY, sampleSummary);
  }

  public void sampleUploadFinished(SampleSummary sampleSummary) throws CTPException {
    sendMessage(UPLOAD_FINISHED_ROUTING_KEY, sampleSummary);
  }

  private void sendMessage(String routingKey, SampleSummary sampleSummary) throws CTPException {
    try {
      String message = this.objectMapper.writeValueAsString(sampleSummary);
      log.debug(
          "Sending message to routing key", kv("routing_key", routingKey), kv("message", message));
      rabbitTemplate.convertAndSend(routingKey, message);
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          String.format("Failed to create JSON message from sample summary - %v", sampleSummary),
          e);
    }
  }
}
