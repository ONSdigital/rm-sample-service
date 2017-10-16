package uk.gov.ons.ctp.response.sample.domain.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

/**
 * JPA Data Repository needed to persist Survey SampleSummarys
 */
@Repository
public interface SampleSummaryRepository extends JpaRepository<SampleSummary, Integer> {

  /**
   * Find Lists of SampleSummary entity by surveyRef and exerciseDateTime
   *
   * @param surveyRef The surveyRef of the SampleSummary
   * @param exerciseDateTime The effectiveStateDateTime of the SampleSummary
   * @param state The state of the SampleSummary
   * @return <List>SampleSummary object or null
   */
  List<SampleSummary> findBySurveyRefAndEffectiveStartDateTimeAndState(String surveyRef, Timestamp exerciseDateTime,
      SampleSummaryDTO.SampleState state);

  /**
   * Find SampleSummary by UUID 
   * 
   * @param id the UUID of the sampleSummary
   * @return SampleSummary object
   */
  SampleSummary findById(UUID id);

}
