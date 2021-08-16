package uk.gov.ons.ctp.response.sample.representation;

import lombok.Data;

@Data
public class SampleUnitParentDTO extends SampleUnitDTO {
  private String collectionExerciseId;
  private SampleUnitChildrenDTO sampleUnitChildrenDTO;
}
