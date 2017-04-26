package uk.gov.ons.ctp.response.sample.message;

import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * Created by wardlk on 20/04/2017.
 */
public interface SampleUnitPublisher {

    /**
     * send sample to collection exercise queue
     * @param sampleUnit to be sent
     */
    void send(SampleUnit sampleUnit);
}
