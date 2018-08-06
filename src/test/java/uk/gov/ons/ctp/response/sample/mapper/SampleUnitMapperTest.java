package uk.gov.ons.ctp.response.sample.mapper;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

public class SampleUnitMapperTest {

  private SampleUnitMapper sampleUnitMapper = new SampleUnitMapper();

  @Test
  public void testSampleAttributeMapping() {
    // Given
    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setFormType("form_type");
    sampleUnit.setId(UUID.randomUUID());
    sampleUnit.setSampleUnitRef("unit_ref");
    sampleUnit.setSampleUnitType("H");

    Map<String, String> attributesMap =
        Collections.singletonMap("test_attribute_key", "test_attribute_value");
    SampleAttributes sampleAttributes = new SampleAttributes(sampleUnit.getId(), attributesMap);
    sampleUnit.setSampleAttributes(sampleAttributes);

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit desinationSampleUnit =
        new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit();

    // When
    sampleUnitMapper.mapAtoB(sampleUnit, desinationSampleUnit, null);

    // Then
    assertEquals(
        "test_attribute_key",
        desinationSampleUnit.getSampleAttributes().getEntries().get(0).getKey());
    assertEquals(
        "test_attribute_value",
        desinationSampleUnit.getSampleAttributes().getEntries().get(0).getValue());
    assertEquals(sampleUnit.getId().toString(), desinationSampleUnit.getId());
    assertEquals(sampleUnit.getFormType(), desinationSampleUnit.getFormType());
    assertEquals(sampleUnit.getSampleUnitType(), desinationSampleUnit.getSampleUnitType());
    assertEquals(sampleUnit.getSampleUnitRef(), desinationSampleUnit.getSampleUnitRef());
  }

  @Test
  public void testMappingNullSampleAttributes() {
    // Given
    SampleUnit sampleUnit = new SampleUnit();
    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit desinationSampleUnit =
        new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit();

    // When
    sampleUnitMapper.mapAtoB(sampleUnit, desinationSampleUnit, null);

    // Then
    assertNull(desinationSampleUnit.getId());
    assertNull(desinationSampleUnit.getSampleAttributes());
    assertNull(desinationSampleUnit.getSampleAttributes());
    assertNull(desinationSampleUnit.getSampleAttributes());
    assertNull(desinationSampleUnit.getFormType());
    assertNull(desinationSampleUnit.getSampleUnitType());
    assertNull(desinationSampleUnit.getSampleUnitRef());
  }
}
