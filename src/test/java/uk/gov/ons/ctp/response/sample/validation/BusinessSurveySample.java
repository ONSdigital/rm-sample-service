package uk.gov.ons.ctp.response.sample.validation;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import libs.sample.validation.BusinessSampleUnit;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Getter
@NoArgsConstructor
public class BusinessSurveySample {

  List<BusinessSampleUnit> sampleUnits;
  @NotNull String surveyRef;
  @NotNull String collectionExerciseRef;
  @NotNull String effectiveStartDateTime;
  @NotNull String effectiveEndDateTime;
}
