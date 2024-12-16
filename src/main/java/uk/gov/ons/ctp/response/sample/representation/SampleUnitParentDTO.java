package uk.gov.ons.ctp.response.sample.representation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SampleUnitParentDTO extends SampleUnitDTO {
  private String collectionExerciseId;
  private SampleUnitChildrenDTO sampleUnitChildrenDTO;
}
