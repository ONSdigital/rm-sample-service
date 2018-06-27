package uk.gov.ons.ctp.response.sample.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;

/** JPA Data Repository needed to persist Survey SampleAttributes */
@Repository
public interface SampleAttributesRepository extends JpaRepository<SampleAttributes, UUID> {}
