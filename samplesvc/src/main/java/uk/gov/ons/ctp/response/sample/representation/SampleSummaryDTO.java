package uk.gov.ons.ctp.response.sample.representation;

import java.sql.Timestamp;

public class SampleSummaryDTO {

  public enum SampleState {
    ACTIVE, INIT,
  }

  private Integer sampleId;

  private Timestamp effectiveStartDateTime;

  private Timestamp effectiveEndDateTime;

  private String surveyRef;

  private Timestamp ingestDateTime;

  private SampleState state;
}
