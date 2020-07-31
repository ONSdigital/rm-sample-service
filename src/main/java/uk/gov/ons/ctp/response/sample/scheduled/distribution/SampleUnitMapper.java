package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.Map;
import ma.glasnost.orika.MapperFacade;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Maps a SampleUnit JPA entity to SampleUnit which can be sent via Rabbit queue */
@Component
public class SampleUnitMapper {
  private MapperFacade mapperFacade;

  public SampleUnitMapper(MapperFacade mapperFacade) {
    this.mapperFacade = mapperFacade;
  }

  /** Convert a SampleUnit */
  public SampleUnit mapSampleUnit(
      uk.gov.ons.ctp.response.sample.domain.model.SampleUnit sampleUnit,
      String collectionExerciseId) {
    SampleUnit mappedSampleUnit = mapperFacade.map(sampleUnit, SampleUnit.class);

    mappedSampleUnit.setId(sampleUnit.getId().toString());
    mappedSampleUnit.setCollectionExerciseId(collectionExerciseId);

    return mappedSampleUnit;
  }
}
