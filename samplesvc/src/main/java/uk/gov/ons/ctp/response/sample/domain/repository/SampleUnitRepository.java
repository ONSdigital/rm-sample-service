package uk.gov.ons.ctp.response.sample.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

import java.util.List;

/**
 * JPA Data Repository needed to persist Survey Sample Units
 */
@Repository
public interface SampleUnitRepository extends JpaRepository<SampleUnit, Integer> {

  /**
   * Find SampleUnit entity by sampleid
   *
   * @param sampleId The sampleId
   * @return SampleUnit object or null
   */
  List<SampleUnit> findBySampleId(Integer sampleId);

  /**
   * Find SampleUnit entity by sampleid
   *
   * @param sampleId The sampleId
   * @return SampleUnit object or null
   */
  Integer countBySampleId(Integer sampleId);
  
}
