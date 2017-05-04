package uk.gov.ons.ctp.response.sample.message.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * The publisher to queues
 */
@Slf4j
@Component
public class SampleUnitPublisherImpl implements SampleUnitPublisher {

    @Qualifier("sampleUnitRabbitTemplate")
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * send sample to collection exercise queue
     * @param sampleUnit to be sent
     */
    @Override
    public void send(SampleUnit sampleUnit) {
        log.debug("send to queue sampleDelivery {}", sampleUnit);
        rabbitTemplate.convertAndSend(sampleUnit);
    }
}
