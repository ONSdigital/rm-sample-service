package uk.gov.ons.ctp.response.sample.party;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Java class for Enrolment. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Enrolment {

  private String enrolmentStatus;
  private String name;
  private String surveyId;
}
