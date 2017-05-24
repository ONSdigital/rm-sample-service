package uk.gov.ons.ctp.response.sample.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

import java.sql.Timestamp;
import java.util.List;

/**
 * JPA Data Repository needed to persist Survey Sample Units
 */
@Repository
public interface SampleUnitRepository extends JpaRepository<SampleUnit, Integer> {

  /**
   * Find SampleUnit entity by samplesummaryfk
   *
   * @param sampleSummaryFK The sampleSummaryFK
   * @return SampleUnit object or null
   */
  List<SampleUnit> findBySampleSummaryFK(Integer sampleSummaryFK);

  /**
   * Count SampleUnit entity by samplesummaryfk
   *
   * @param sampleSummaryFK The sampleSummaryFK
   * @return Integer number of SampleUnits with sampleSummaryFK
   */
  Integer countBySampleSummaryFK(Integer sampleSummaryFK);

  /**
   * find list of samples by sampleSummaryFK, surveyref, exerciseDateTime, and state limited by count
   * @param surveyRef to find by
   * @param excerciseDateTime to find by
   * @param state state to search by
   * @param count configurable number of entries to fetch
   * @return SampleUnit list of mathcing sample units or null if not found
   */
  @Query(value = "SELECT su.* FROM sample.sampleunit su ,sample.samplesummary ss WHERE su.samplesummaryfk = ss.samplesummarypk "
          + "AND ss.effectivestartdatetime = :exercisedatetime AND ss.state = :state AND su.state = 'INIT' "
          + "AND ss.surveyref = :surveyref AND su.sampleunitpk  NOT IN ( :excludedcases ) order by ss.ingestdatetime ASC limit :count  ;",
          nativeQuery = true)
  List<SampleUnit> getSampleUnitBatch(@Param("surveyref") String surveyRef,
                                      @Param("exercisedatetime") Timestamp excerciseDateTime,
                                      @Param("state") String state,
                                      @Param("count") Integer count,
                                      @Param("excludedcases") List<Integer> excludedCases);

}
