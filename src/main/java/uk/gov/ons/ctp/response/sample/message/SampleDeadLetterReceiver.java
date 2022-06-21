package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.feedback.SampleDeadLetter;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryEnrichmentService;

/** The reader of CaseReceipts from queue */
@MessageEndpoint
public class SampleDeadLetterReceiver {
  private static final Logger log = LoggerFactory.getLogger(SampleDeadLetterReceiver.class);
  @Autowired private SampleService sampleService;
  @Autowired SampleSummaryEnrichmentService sampleSummaryEnrichmentService;
  /**
   * To process SampleSummaries from dead letter queue
   *
   * @param sampleDeadLetter to process
   * @throws CTPException CTPException
   */
//  @Transactional()
  public void process(SampleDeadLetter sampleDeadLetter) throws CTPException {
    log.info("Processing dead letter sample", kv("dead letter sample", sampleDeadLetter));
    UUID sampleSummaryId = UUID.fromString(sampleDeadLetter.getSampleSummaryId());

    SampleSummary existingSampleSummary = sampleService.findSampleSummary(sampleSummaryId);
    log.info("Found existing sample summary", kv("existing_sample_summary", existingSampleSummary));

    if (existingSampleSummary == null) {
      log.error("No existing sample summary found", kv("sample_summary_id", sampleSummaryId));
    } else {
      sampleSummaryEnrichmentService.failSampleSummary(sampleSummaryId);
    }
    log.info("Dead letter sample processing complete", kv("sample_summary", sampleDeadLetter));
  }
}
