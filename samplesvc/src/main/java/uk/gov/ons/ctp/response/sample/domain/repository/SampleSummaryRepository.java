package uk.gov.ons.ctp.response.sample.domain.repository;

import java.sql.Timestamp;
import java.util.List;

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
  
  /**
   * Find Lists of SampleSummary entity by surveyRef and exerciseDateTime
   *
   * @param surveyRef The surveyRef 
   * @param exerciseDateTime The effectiveStateDateTime 
   * @return <List>SampleSummary object or null
   */
  List<SampleSummary> findBySurveyRefAndEffectiveStartDateTime(String surveyRef, Timestamp exerciseDateTime);
  
}
