package uk.gov.ons.ctp.response.sample.domain.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/** JPA Data Repository needed to persist Survey Sample Units */
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

  Stream<SampleUnit> findBySampleSummaryFKAndState(
      Integer sampleSummaryFK, SampleUnitDTO.SampleUnitState state);

  /**
   * find SampleUnit by sampleUnitRef and sampleUnitType from Party object
   *
   * @param sampleUnitRef sampleUnitRef of Party
   * @param sampleUnitType sampleUnitType of Party
   * @return SampleUnit with required type/ref combo
   */
  SampleUnit findBySampleUnitRefAndAndSampleUnitType(String sampleUnitRef, String sampleUnitType);

  /**
   * Get the sample unit object based on the sample unit reference
   *
   * @param sampleUnitRef
   * @return
   */
  SampleUnit findBySampleUnitRef(String sampleUnitRef);

  /**
   * Find how many SampleUnits from a given SampleSummary have been POSTed to Party and are now
   * 'PERSISTED'
   *
   * @param sampleSummaryFK sampleSummaryFK of SampleSummary to be counted, from a SampleUnit in
   *     that summary.
   * @return int count of matching sampleUnits
   */
  int countBySampleSummaryFKAndState(Integer sampleSummaryFK, SampleUnitDTO.SampleUnitState state);

  SampleUnit findById(UUID id);
}
