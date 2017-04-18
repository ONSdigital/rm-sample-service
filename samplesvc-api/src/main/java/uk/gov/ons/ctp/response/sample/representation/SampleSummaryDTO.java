package uk.gov.ons.ctp.response.sample.representation;

import java.sql.Timestamp;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)

public class SampleSummaryDTO {

  /**
   * enum for Sample state
   */
  public enum SampleState {
    ACTIVE, INIT,
  }

  /**
   * enum for Sample event
   */
  public enum SampleEvent {
    ACTIVATED
  }

  private Integer sampleId;

  private Timestamp effectiveStartDateTime;

  private Timestamp effectiveEndDateTime;

  private String surveyRef;

  private Timestamp ingestDateTime;

  private SampleState state;
}
