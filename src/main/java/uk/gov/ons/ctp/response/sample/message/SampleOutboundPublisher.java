package uk.gov.ons.ctp.response.sample.message;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

/**
 * An interface for publishing notifications that a sample has been uploaded
 */
public interface SampleOutboundPublisher {
    void sampleUploadStarted(SampleSummary sampleSummary) throws CTPException;
    void sampleUploadFinished(SampleSummary sampleSummary) throws CTPException;
}
