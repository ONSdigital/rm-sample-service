package libs.survey.representation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SurveyClassifier API representation */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SurveyClassifierDTO {

  private String id;
  private String name;
}
