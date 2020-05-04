package uk.gov.ons.ctp.response.sample.validation;

import javax.validation.constraints.NotNull;
import lombok.*;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SurveyBase {

  @NotNull String surveyRef;

  @NotNull String collectionExerciseRef;

  @NotNull String effectiveStartDateTime;

  @NotNull String effectiveEndDateTime;
}
