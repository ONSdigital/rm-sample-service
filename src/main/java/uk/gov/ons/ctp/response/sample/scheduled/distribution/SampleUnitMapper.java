package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Map;
import ma.glasnost.orika.MapperFacade;

import org.mortbay.log.Log;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.SampleAttributes;

/** Maps a SampleUnit JPA entity to SampleUnit which can be sent via Rabbit queue */
@Component
public class SampleUnitMapper {
  private SampleAttributesRepository sampleAttributesRepository;
  private MapperFacade mapperFacade;

  public SampleUnitMapper(
      SampleAttributesRepository sampleAttributesRepository, MapperFacade mapperFacade) {
    this.sampleAttributesRepository = sampleAttributesRepository;
    this.mapperFacade = mapperFacade;
  }

  /** Convert a SampleUnit */
  public SampleUnit mapSampleUnit(
      uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit,
      String collectionExerciseId) {
    Log.debug("Mapping SampleUnit from domain model", kv("SampleUnit", sampleUnit));
    SampleUnit mappedSampleUnit = mapperFacade.map(sampleUnit, SampleUnit.class);
    Log.info("SampleUnit mapped from domain model", kv("MappedSampleUnit", mappedSampleUnit));

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
