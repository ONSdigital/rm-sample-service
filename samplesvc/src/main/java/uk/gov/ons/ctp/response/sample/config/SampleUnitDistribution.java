package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;

/**
 * Application config bean for the connection details to the Sample Service for SampleUnits.
 */
@Data
public class SampleUnitDistribution {
  private Integer retrievalMax;
  private Integer distributionMax;
  private Integer retrySleepSeconds;
  private Integer delayMilliSeconds;
}
