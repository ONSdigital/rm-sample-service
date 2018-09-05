package uk.gov.ons.ctp.response.sample.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
    log.with("report_result", reportResult).debug("Just ran the chasing report and got result");
  }
}
