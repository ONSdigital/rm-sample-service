package uk.gov.ons.ctp.response.sample.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;

/**
 * JPA Data Repository needed to persist Survey SampleSummarys
 */
@Repository
public interface SampleSummaryRepository extends JpaRepository<SampleSummary, Integer> {
  
  /**
   * Find SampleSummary entity by sampleid
   *
   * @param sampleId The sampleId 
   * @return SampleSummary object or null
   */
  SampleSummary findBySampleId(Integer sampleId);
  
}
