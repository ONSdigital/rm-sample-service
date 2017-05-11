package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;

@Data
public class SampleUnitDistribution {
  private Integer retrievalMax;
  private Integer distributionMax;
  private Integer retrySleepSeconds;
  private Integer delayMilliSeconds;
}
