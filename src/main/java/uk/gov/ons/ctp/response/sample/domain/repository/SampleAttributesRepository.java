package uk.gov.ons.ctp.response.sample.domain.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;

/** JPA Data Repository needed to persist Survey SampleAttributes */
@Repository
public interface SampleAttributesRepository extends JpaRepository<SampleAttributes, UUID> {

  @Query(
      value =
          "select sa.* from sample.sampleattributes sa where "
              + "UPPER(REPLACE(attributes->>'POSTCODE', ' ', '')) = "
              + "UPPER(REPLACE(:postcode, ' ', ''))",
      nativeQuery = true)
  public List<SampleAttributes> findByPostcode(@Param("postcode") String postcode);
}
