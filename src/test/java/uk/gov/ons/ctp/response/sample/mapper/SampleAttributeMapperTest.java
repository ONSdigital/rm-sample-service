package uk.gov.ons.ctp.response.sample.mapper;

import org.junit.Test;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.representation.SampleAttributesDTO;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SampleAttributeMapperTest {

  private SampleAttributeMapper mapper = new SampleAttributeMapper();

  @Test
  public void testMapAttributes() {
    // Given
    SampleAttributes sampleAttributes =
        new SampleAttributes(UUID.randomUUID(), Collections.singletonMap("key", "value"));
    SampleAttributesDTO sampleAttributesDTO = new SampleAttributesDTO();

    // When
    mapper.mapAtoB(sampleAttributes, sampleAttributesDTO, null);

    // Then
    assertThat(sampleAttributesDTO.getId()).isEqualTo(sampleAttributes.getSampleUnitFK());
    assertThat(sampleAttributesDTO.getAttributes()).isEqualTo(sampleAttributes.getAttributes());
  }
}
