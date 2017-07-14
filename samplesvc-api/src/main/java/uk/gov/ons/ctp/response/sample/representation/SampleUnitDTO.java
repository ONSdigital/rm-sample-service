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
    INIT, DELIVERED, PERSISTED
  }

  /**
   * enum for SampleUnit event
   */
  public enum SampleUnitEvent {
    DELIVERING, PERSISTING
  }

  /**
   * enum for SampleUnit type
   */
  public enum SampleUnitType {
    H(true), HI(false), C(true), CI(false), B(true), BI(false);
    
    private boolean isParent;
    
    SampleUnitType(boolean isParent) {
      this.isParent = isParent;
    }
    
    public boolean isParent() {
      return isParent;
    }
  }

  private Integer sampleUnitPK;

  private Integer sampleSummaryFK;

  private String sampleUnitRef;

  private String sampleUnitType;

  private String formType;

  private SampleUnitState state;

}
