package uk.gov.ons.ctp.response.sample.representation;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleUnit {
  protected String id;
  @NotNull protected String sampleUnitRef;
  @NotNull protected String sampleUnitType;
  protected String formType;
  @NotNull protected String collectionExerciseId;
}
