package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.common.health.ScheduledHealthInfo;

/**
 * Information object for SampleUnit Distributor
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SampleUnitDistributionInfo extends ScheduledHealthInfo {
  private int sampleUnitsSucceeded;
  private int sampleUnitsFailed;
}
