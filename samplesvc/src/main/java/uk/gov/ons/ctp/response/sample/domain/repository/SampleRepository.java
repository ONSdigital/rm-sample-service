package uk.gov.ons.ctp.response.sample.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.Sample;

/**
 * JPA Data Repository needed to persist IAC records
 */
@Repository
public interface SampleRepository extends JpaRepository<Sample, Integer> {

}
