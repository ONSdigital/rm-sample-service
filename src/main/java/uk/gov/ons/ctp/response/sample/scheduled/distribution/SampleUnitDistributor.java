package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Distributes SampleUnits to Collex when requested via job. Retries failures until successful */
@Component
@Slf4j
public class SampleUnitDistributor {
  private static final String LOCK_PREFIX = "SampleCollexJob-";

  @Autowired private AppConfig appConfig;

  @Autowired private SampleUnitSender sampleUnitSender;

  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private RedissonClient redissonClient;

  @Autowired private SampleUnitMapper sampleUnitMapper;

  /** Scheduled job for distributing SampleUnits */
  @Scheduled(fixedDelayString = "#{appConfig.sampleUnitDistribution.delayMilliSeconds}")
  @Transactional
  public void distribute() {
    List<CollectionExerciseJob> jobs = collectionExerciseJobRepository.findByJobCompleteIsFalse();

    for (CollectionExerciseJob job : jobs) {
      String uniqueLockName = LOCK_PREFIX + job.getCollectionExerciseJobPK();

      RLock lock = redissonClient.getFairLock(uniqueLockName);

      try {
        // Wait for a lock. Automatically unlock after a certain amount of time to prevent issues
        // when lock holder crashes or Redis crashes causing permanent lockout
        if (lock.tryLock(
            appConfig.getDataGrid().getLockTimeToWaitSeconds(),
            appConfig.getDataGrid().getLockTimeToLiveSeconds(),
            TimeUnit.SECONDS)) {
          try {
            processJob(job);
          } finally {
            // Always unlock the distributed lock
            lock.unlock();
          }
        }
      } catch (InterruptedException e) {
        // Ignored - process stopped while waiting for lock
      }
    }
  }

  private void processJob(CollectionExerciseJob job) {
    List<SampleUnit> mappedSampleUnits =
        getMappedSampleUnits(job.getSampleSummaryId(), job.getCollectionExerciseId().toString());

    boolean hasErrors = false;

    for (SampleUnit msu : mappedSampleUnits) {
      try {
        sampleUnitSender.sendSampleUnit(msu);
      } catch (CTPException e) {
        hasErrors = true;
        log.error(
            "Failed to send a sample unit to queue and update state with ID: {}", msu.getId(), e);
      }
    }

    if (!hasErrors) {
      job.setJobComplete(true);
      collectionExerciseJobRepository.saveAndFlush(job);
    }
  }

  private List<SampleUnit> getMappedSampleUnits(UUID sampleSummaryId, String collectionExerciseId) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId);

    if (sampleSummary.getState() != SampleState.ACTIVE) {
      return Collections.EMPTY_LIST;
    }

    List<SampleUnit> mappedSampleUnits = new LinkedList<>();

    try (Stream<uk.gov.ons.ctp.response.sample.domain.model.SampleUnit> sampleUnits =
        sampleUnitRepository.findBySampleSummaryFKAndState(
            sampleSummary.getSampleSummaryPK(), SampleUnitState.PERSISTED)) {
      sampleUnits.forEach(
          su -> {
            SampleUnit mappedSampleUnit = sampleUnitMapper.mapSampleUnit(su, collectionExerciseId);

            mappedSampleUnits.add(mappedSampleUnit);
          });
    }

    return mappedSampleUnits;
  }
}
