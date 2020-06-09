package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleReportRepository;

/**
 * A SampleReportService implementation which encapsulates all business logic operating on the
 * SampleReport entity model.
 */
@Service
public class SampleReportService {
  private static final Logger log = LoggerFactory.getLogger(SampleReportService.class);

  @Autowired private SampleReportRepository sampleReportRepository;

  /** Creates a Report */
  public void createReport() {
    log.debug("Entering createReport...");

    boolean reportResult = sampleReportRepository.chasingReportStoredProcedure();
    log.debug("Just ran the chasing report and got result", kv("report_result", reportResult));
  }
}
