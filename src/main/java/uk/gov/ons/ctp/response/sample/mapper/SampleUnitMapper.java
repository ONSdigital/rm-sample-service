package uk.gov.ons.ctp.response.sample.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

public class SampleUnitMapper
    extends CustomMapper<uk.gov.ons.ctp.response.sample.domain.model.SampleUnit, SampleUnitDTO> {

  @Override
  public void mapAtoB(
      uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit,
      SampleUnitDTO sampleUnit2,
      MappingContext context) {
    sampleUnit2.setFormType(sampleUnit.getFormType());
    sampleUnit2.setSampleUnitRef(sampleUnit.getSampleUnitRef());
    sampleUnit2.setSampleUnitType(sampleUnit.getSampleUnitType());
  }
}
