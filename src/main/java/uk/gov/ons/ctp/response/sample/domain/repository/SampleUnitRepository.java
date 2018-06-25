package uk.gov.ons.ctp.response.sample.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

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
   * @param excludedCases Cases excluded
   * @return SampleUnit list of mathcing sample units or null if not found
   */
  @Deprecated
  @Query(value = "SELECT su.* FROM sample.sampleunit su ,sample.samplesummary ss WHERE "
          + "su.samplesummaryfk = ss.samplesummarypk AND ss.effectivestartdatetime = :exercisedatetime "
          + "AND ss.statefk = :state AND su.statefk = 'PERSISTED' AND ss.surveyref = :surveyref AND "
          + "su.sampleunitpk  NOT IN ( :excludedcases ) order by ss.ingestdatetime ASC limit :count  ;",
          nativeQuery = true)
  List<SampleUnit> getSampleUnitBatch(@Param("surveyref") String surveyRef,
                                      @Param("exercisedatetime") Timestamp excerciseDateTime,
                                      @Param("state") String state,
                                      @Param("count") Integer count,
                                      @Param("excludedcases") List<Integer> excludedCases);

  /**
   * find list of samples by sampleSummaryFK, surveyref, exerciseDateTime, and state limited by count
   * @param state state to search by
   * @return SampleUnit list of mathcing sample units or null if not found
   */
  @Query(value = "SELECT su.* FROM sample.sampleunit su ,sample.samplesummary ss WHERE "
      + "ss.samplesummarypk = su.samplesummaryfk "
      + "AND ss.id = :samplesummary "
      + "AND ss.statefk = :state "
      + "AND su.statefk = 'PERSISTED' "
      + "AND su.sampleunitpk NOT IN ( :excludedcases ) order by ss.ingestdatetime ASC limit :count  ;",
      nativeQuery = true)
  List<SampleUnit> getSampleUnits(@Param("samplesummary") UUID sampleSummaryFK,
      @Param("state") String state,
      @Param("count") Integer count,
      @Param("excludedcases") List<Integer> excludedCases);

  /**
   * find SampleUnit by sampleUnitRef and sampleUnitType from Party object
   * @param sampleUnitRef sampleUnitRef of Party
   * @param sampleUnitType sampleUnitType of Party
   * @return SampleUnit with required type/ref combo
   */
  @Query(value = "SELECT su.* FROM sample.sampleunit su WHERE "
          + "su.sampleunitref = :sampleunitref AND su.sampleunittype = :sampleunittype ;",
          nativeQuery = true)
  SampleUnit findBySampleUnitRefAndType(@Param("sampleunitref") String sampleUnitRef,
                                        @Param("sampleunittype") String sampleUnitType);

  /**
   * Get the sample unit object based on the sample unit reference
   * @param sampleUnitRef
   * @return
   */
  SampleUnit findBySampleUnitRef(String sampleUnitRef);

  /**
   * Find how many SampleUnits from a given SampleSummary have been POSTed to Party and are now 'PERSISTED'
   * @param sampleSummaryFK sampleSummaryFK of SampleSummary to be counted, from a SampleUnit in that summary.
   * @return int count of matching sampleUnits
   */
  @Query(value = "SELECT COUNT(sampleUnitPK) FROM sample.sampleunit su WHERE su.samplesummaryfk = :samplesummaryfk "
          + "AND su.statefk = 'PERSISTED' ;",
          nativeQuery = true)
  int getPartiedForSampleSummary(@Param("samplesummaryfk") int sampleSummaryFK);

  /**
   * Find total amount of sampleUnits in a SampleSummary
   * @param sampleSummaryFK SampleSummaryFK of SampleSummary to be counted, from a SampleUnit in that summary.
   * @return int count of SampleUnits in SampleSummary
   */
  @Query(value = "SELECT COUNT(sampleUnitPK) FROM sample.sampleunit su WHERE su.samplesummaryfk = :samplesummaryfk ;",
          nativeQuery = true)
  int getTotalForSampleSummary(@Param("samplesummaryfk") int sampleSummaryFK);

  SampleUnit findById(UUID id);
}
