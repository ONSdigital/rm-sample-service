package uk.gov.ons.ctp.response.sample.message;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** The publisher to queues */
@CoverageIgnore
@Slf4j
@MessageEndpoint
public class SampleUnitPublisher {

  @Qualifier("sampleUnitRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * send sample to collection exercise queue
   *
   * @param sampleUnit to be sent
   */
  public void send(SampleUnit sampleUnit) {
    log.debug("send to queue sampleDelivery {}", sampleUnit);
    rabbitTemplate.convertAndSend(sampleUnit);
  }
}
