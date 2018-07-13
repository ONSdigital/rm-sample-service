package uk.gov.ons.ctp.response.sample.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import uk.gov.ons.ctp.response.sample.representation.SampleAttributesDTO;

public class SampleAttributeMapper
    extends CustomMapper<
        uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes, SampleAttributesDTO> {

  @Override
  public void mapAtoB(
      uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes,
      SampleAttributesDTO sampleAttributesDTO,
      MappingContext context) {
    sampleAttributesDTO.setId(sampleAttributes.getSampleUnitFK());
    sampleAttributesDTO.setAttributes(sampleAttributes.getAttributes());
  }
}
