package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import net.sourceforge.cobertura.CoverageIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** The publisher to queues */
@CoverageIgnore
@MessageEndpoint
public class SampleUnitPublisher {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitPublisher.class);

  @Qualifier("sampleUnitRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * send sample to collection exercise queue
   *
   * @param sampleUnit to be sent
   */
  public void send(SampleUnit sampleUnit) {
    log.debug("send to queue sampleDelivery", kv("sample_unit", sampleUnit));
    rabbitTemplate.convertAndSend(sampleUnit);
  }
}
