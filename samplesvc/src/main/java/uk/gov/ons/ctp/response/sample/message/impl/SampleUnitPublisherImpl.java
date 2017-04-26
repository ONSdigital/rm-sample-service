package uk.gov.ons.ctp.response.sample.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The publisher to queues
 */
@Slf4j
@Named
public class SampleUnitPublisherImpl implements SampleUnitPublisher {

    @Qualifier("sampleUnitRabbitTemplate")
    @Inject
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
