package uk.gov.ons.ctp.response.sample.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

/** JPA Data Repository needed to persist Survey SampleSummarys */
@Repository
public interface SampleSummaryRepository extends JpaRepository<SampleSummary, Integer> {

  /**
   * Find SampleSummary by UUID
   *
   * @param id the UUID of the sampleSummary
   * @return SampleSummary object
   */
  List<SampleSummary> findById(UUID id);

  Optional<SampleSummary> findBySampleSummaryPK(Integer sampleSummaryPK);
}
