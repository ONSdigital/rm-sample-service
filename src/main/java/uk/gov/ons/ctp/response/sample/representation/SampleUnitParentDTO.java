package uk.gov.ons.ctp.response.sample.representation;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

@Data
@SuperBuilder
public class SampleUnitParentDTO extends SampleUnit {
  protected String collectionExerciseId;
  protected SampleUnitChildrenDTO sampleUnitChildrenDTO;
}
