package uk.gov.ons.ctp.response.sample.domain.repository;

import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;
import uk.gov.ons.ctp.response.sample.domain.model.SampleReport;

/** The repository used to trigger stored procedure execution. */
public interface SampleReportRepository extends JpaRepository<SampleReport, UUID> {
  /**
   * To execute ggenerate_sample_mi
   *
   * @return boolean whether report has been created successfully
   */
  @Modifying
  @Transactional
  @Procedure(name = "SampleReport.generateReport")
  Boolean chasingReportStoredProcedure();
}
