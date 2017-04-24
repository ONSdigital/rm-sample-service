package uk.gov.ons.ctp.response.sample.representation;

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

public class SampleUnitDTO {

  /**
   * enum for SampleUnit state
   */
  public enum SampleUnitState {
    INIT, DELIVERED,
  }

  private Integer sampleUnitId;

  private Integer sampleId;

  private String sampleUnitRef;

  private String sampleUnitType;

  private String formType;

  private SampleUnitState state;
}
