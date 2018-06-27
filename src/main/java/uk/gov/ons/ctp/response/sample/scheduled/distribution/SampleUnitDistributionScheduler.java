package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Schedules SampleUnit Distribution */
@CoverageIgnore
@Service
@Slf4j
public class SampleUnitDistributionScheduler implements HealthIndicator {

  @Autowired private SampleUnitDistributor sampleUnitDistributorImpl;
  private SampleUnitDistributionInfo distribInfo = new SampleUnitDistributionInfo();

  @Override
  public Health health() {
    return Health.up().withDetail("activationInfo", distribInfo).build();
  }

  /** Scheduled Runner for distributing SampleUnits */
  @Scheduled(fixedDelayString = "#{appConfig.sampleUnitDistribution.delayMilliSeconds}")
  public void run() {
    try {
      distribInfo = sampleUnitDistributorImpl.distribute();
    } catch (Exception e) {
      log.error("Exception in case distributor", e);
    }
  }
}
