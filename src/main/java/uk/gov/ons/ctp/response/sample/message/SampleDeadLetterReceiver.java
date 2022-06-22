package uk.gov.ons.ctp.response.sample.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryEnrichmentService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;

/** The reader of dead lettered samples from queue */
@MessageEndpoint
public class SampleDeadLetterReceiver {
  private static final Logger log = LoggerFactory.getLogger(SampleDeadLetterReceiver.class);
  @Autowired private SampleService sampleService;
  @Autowired SampleSummaryEnrichmentService sampleSummaryEnrichmentService;
  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent>
      sampleSummaryTransitionManager;
  /**
   * To process SampleSummaries from dead letter queue
   *
   * @param sampleDeadLetterId to process
   * @throws CTPException CTPException
   */
  @Transactional(propagation = Propagation.REQUIRED, value = "transactionManager")
  public void process(UUID sampleDeadLetterId)
      throws CTPException, UnknownSampleSummaryException, RuntimeException {
    log.info("Processing dead letter sample", kv("dead letter sample", sampleDeadLetterId));

    SampleSummary existingSampleSummary = sampleService.findSampleSummary(sampleDeadLetterId);
    log.info("Found existing sample summary", kv("existing_sample_summary", existingSampleSummary));

    if (existingSampleSummary == null) {
      log.error("No existing sample summary found", kv("sample_summary_id", sampleDeadLetterId));
    } else {
      log.info("failing sample summary", kv("sampleSummaryId", sampleDeadLetterId));
      SampleSummary sampleSummary =
          sampleSummaryRepository
              .findById(sampleDeadLetterId)
              .orElseThrow(UnknownSampleSummaryException::new);

      SampleSummaryDTO.SampleState newState =
          sampleSummaryTransitionManager.transition(
              sampleSummary.getState(), SampleSummaryDTO.SampleEvent.FAIL_INGESTION);
      sampleSummary.setState(newState);
      sampleSummary.setNotes("Error: Sample file formatting problem, potentially invalid number of business columns");
      this.sampleSummaryRepository.save(sampleSummary);
      log.info("sample summary transitioned to failed", kv("sampleSummaryId", sampleDeadLetterId));
    }
    log.info("Dead letter sample processing complete", kv("sample_summary", sampleDeadLetterId));
  }
}
