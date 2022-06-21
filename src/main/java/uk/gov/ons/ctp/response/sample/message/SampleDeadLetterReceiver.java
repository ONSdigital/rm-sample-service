package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
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
   * @param sampleDeadLetterId to process
   * @throws CTPException CTPException
   */
  //  @Transactional()
  public void process(UUID sampleDeadLetterId) throws CTPException {
    log.info("Processing dead letter sample", kv("dead letter sample", sampleDeadLetterId));

    SampleSummary existingSampleSummary = sampleService.findSampleSummary(sampleDeadLetterId);
    log.info("Found existing sample summary", kv("existing_sample_summary", existingSampleSummary));

    if (existingSampleSummary == null) {
      log.error("No existing sample summary found", kv("sample_summary_id", sampleDeadLetterId));
    } else {
      sampleSummaryEnrichmentService.failSampleSummary(sampleDeadLetterId);
    }
    log.info("Dead letter sample processing complete", kv("sample_summary", sampleDeadLetterId));
  }
}
