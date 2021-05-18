package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static net.logstash.logback.argument.StructuredArguments.kv;

import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Maps a SampleUnit JPA entity to SampleUnit which can be sent via Rabbit queue */
@Component
public class SampleUnitMapper {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitMapper.class);

  private MapperFacade mapperFacade;

  public SampleUnitMapper(MapperFacade mapperFacade) {
    this.mapperFacade = mapperFacade;
  }

  /** Convert a SampleUnit */
  public SampleUnit mapSampleUnit(
      uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit,
      String collectionExerciseId) {
    log.debug("Mapping SampleUnit from domain model", kv("SampleUnit", sampleUnit));
    SampleUnit mappedSampleUnit = mapperFacade.map(sampleUnit, SampleUnit.class);
    log.info("SampleUnit mapped from domain model", kv("MappedSampleUnit", mappedSampleUnit));

    mappedSampleUnit.setId(sampleUnit.getId().toString());
    mappedSampleUnit.setCollectionExerciseId(collectionExerciseId);

    return mappedSampleUnit;
  }
}
