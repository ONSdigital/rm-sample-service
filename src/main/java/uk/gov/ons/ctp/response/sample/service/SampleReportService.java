package uk.gov.ons.ctp.response.sample.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleReportRepository;

/**
 * A SampleReportService implementation which encapsulates all business logic operating on the
 * SampleReport entity model.
 */
@Service
@Slf4j
public class SampleReportService {

  @Autowired private SampleReportRepository sampleReportRepository;

  /** Creates a Report */
  public void createReport() {
    log.debug("Entering createReport...");

    boolean reportResult = sampleReportRepository.chasingReportStoredProcedure();
    log.debug("Just ran the chasing report and result is {}", reportResult);
  }
}
