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
    INIT, DELIVERED
  }

  /**
   * enum for SampleUnit event
   */
  public enum SampleUnitEvent {
    DELIVERING
  }

  /**
   * enum for SampleUnit type
   */
  //TODO would be better to have (HOUSEHOLD,"H") and get CaseSvc to map with JPA so that in db the value is "H", but code refers to HOUSEHOLD
  public enum SampleUnitType {
    H, HI, C, CI, B, BI
  }

  private Integer sampleUnitPK;

  private Integer sampleSummaryFK;

  private String sampleUnitRef;

  private String sampleUnitType;

  private String formType;

  private SampleUnitState state;

}
