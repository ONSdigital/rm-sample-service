package uk.gov.ons.ctp.response.sample.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import uk.gov.ons.ctp.response.libs.SampleUnit;

public class SampleUnitMapper
    extends CustomMapper<uk.gov.ons.ctp.response.sample.domain.model.SampleUnit, SampleUnit> {

  @Override
  public void mapAtoB(
      uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit,
      SampleUnit sampleUnit2,
      MappingContext context) {
    sampleUnit2.setFormType(sampleUnit.getFormType());
    sampleUnit2.setSampleUnitRef(sampleUnit.getSampleUnitRef());
    sampleUnit2.setSampleUnitType(sampleUnit.getSampleUnitType());
    if (sampleUnit.getSampleAttributes() != null) {
      SampleUnit.SampleAttributes.Builder<Void> builder =
          new SampleUnit.SampleAttributes().newCopyBuilder();
      sampleUnit
          .getSampleAttributes()
          .getAttributes()
          .forEach((k, v) -> builder.addEntries().withKey(k).withValue(v));
      sampleUnit2.setSampleAttributes(builder.build());
    }
  }
}
