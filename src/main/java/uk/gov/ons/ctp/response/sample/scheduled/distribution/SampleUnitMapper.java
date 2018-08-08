package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.Map;
import ma.glasnost.orika.MapperFacade;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.SampleAttributes;

@Component
public class SampleUnitMapper {
  private SampleAttributesRepository sampleAttributesRepository;
  private MapperFacade mapperFacade;

  public SampleUnitMapper(
      SampleAttributesRepository sampleAttributesRepository, MapperFacade mapperFacade) {
    this.sampleAttributesRepository = sampleAttributesRepository;
    this.mapperFacade = mapperFacade;
  }

  public uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mapSampleUnit(
      SampleUnit sampleUnit, String collectionExerciseId) {
    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
        mapperFacade.map(
            sampleUnit, uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);

    uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes =
        sampleAttributesRepository.findOne(sampleUnit.getId());

    if (sampleAttributes != null) {
      mappedSampleUnit.setSampleAttributes(mapSampleAttributes(sampleAttributes));
    }

    mappedSampleUnit.setId(sampleUnit.getId().toString());
    mappedSampleUnit.setCollectionExerciseId(collectionExerciseId);

    return mappedSampleUnit;
  }

  private SampleAttributes mapSampleAttributes(
      uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes sampleAttributes) {
    SampleAttributes mappedSampleAttributes = new SampleAttributes();
    SampleAttributes.Builder<Void> sampleAttributesBuilder =
        mappedSampleAttributes.newCopyBuilder();
    for (Map.Entry<String, String> attribute : sampleAttributes.getAttributes().entrySet()) {
      sampleAttributesBuilder
          .addEntries()
          .withKey(attribute.getKey())
          .withValue(attribute.getValue());
    }
    return sampleAttributesBuilder.build();
  }
}
