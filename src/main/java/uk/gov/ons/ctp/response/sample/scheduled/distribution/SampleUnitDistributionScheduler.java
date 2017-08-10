package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Schedules SampleUnit Distribution
 */
@CoverageIgnore
@Service
@Slf4j
public class SampleUnitDistributionScheduler implements HealthIndicator {

  @Override
  public Health health() {
    return Health.up()
        .withDetail("activationInfo", distribInfo)
        .build();
  }

  @Autowired
  private SampleUnitDistributor sampleUnitDistributorImpl;

  private SampleUnitDistributionInfo distribInfo = new SampleUnitDistributionInfo();

  /**
   * Scheduled Runner for distributing SampleUnits
   */
  @Scheduled(fixedDelayString = "#{appConfig.sampleUnitDistribution.delayMilliSeconds}")
  public void run() {
    try {
      distribInfo = sampleUnitDistributorImpl.distribute();
    } catch (Exception e) {
      log.error("Exception in case distributor", e);
    }
  }

}
