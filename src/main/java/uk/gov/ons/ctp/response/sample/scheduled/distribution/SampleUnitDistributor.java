package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

/** Distributes SampleUnits */
@Component
@Slf4j
public class SampleUnitDistributor {

  private static final ExecutorService executor = Executors.newSingleThreadExecutor();
  private static final Map<UUID, Pair<RLock, Future<Boolean>>> workInProgress = new HashMap<>();

  @Autowired private SampleUnitSenderFactory sampleUnitSenderFactory;

  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private RedissonClient redissonClient;

  @Autowired private SampleUnitMapper sampleUnitMapper;

  /** Scheduled job for distributing SampleUnits */
  @Scheduled(fixedDelayString = "#{appConfig.sampleUnitDistribution.delayMilliSeconds}")
  public void run() {
    distribute();
  }

  @Transactional
  public void distribute() {
    List<CollectionExerciseJob> jobs = collectionExerciseJobRepository.findByJobCompleteIsFalse();

    for (CollectionExerciseJob job : jobs) {
      RLock lock =
          redissonClient.getFairLock("SampleCollexJob-" + job.getCollectionExerciseJobPK());

      try {
        // Wait up to 30 seconds for a lock. Automatically unlock after 10 minutes to prevent
        // issues when lock holder crashes or Redis crashes causing permanent lockout
        if (lock.tryLock(30, 600, TimeUnit.SECONDS)) {
          processJob(job, lock);
        }
      } catch (InterruptedException e) {
        // Ignored
      }
    }
  }

  private void processJob(CollectionExerciseJob job, RLock lock) {
    Pair<RLock, Future<Boolean>> wip =
        workInProgress.computeIfAbsent(
            job.getSampleSummaryId(),
            key -> {
              List<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> mappedSampleUnits =
                  getMappedSampleUnits(key, job.getCollectionExerciseId().toString());

              SampleUnitSender sender =
                  sampleUnitSenderFactory.getNewSampleUnitSender(mappedSampleUnits);

              Future<Boolean> future = executor.submit(sender);

              return Pair.of(lock, future);
            });

    // Really small delay because there's no point checking the thread so quickly after
    // we started it
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      // Ignored
    }

    try {
      Future<Boolean> wipFuture = wip.getSecond();

      if (wipFuture.isDone() && wipFuture.get() == Boolean.TRUE) {
        job.setJobComplete(true);
        collectionExerciseJobRepository.saveAndFlush(job);
      }

      if (wipFuture.isDone() || wipFuture.isCancelled()) {
        workInProgress.remove(job.getSampleSummaryId());

        // Finally, we can unlock the distributed lock
        wip.getFirst().unlock();
      }
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error checking results of worker threads. Service is probably shutting down", e);
    }
  }

  private List<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> getMappedSampleUnits(
      UUID sampleSummaryId, String collectionExerciseId) {

    List<uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit> mappedSampleUnits =
        new LinkedList<>();

    try (Stream<SampleUnit> sampleUnits =
        sampleUnitRepository.findBySampleSummaryFKAndState(
            sampleSummaryId, SampleUnitState.PERSISTED)) {
      sampleUnits.forEach(
          su -> {
            uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
                sampleUnitMapper.mapSampleUnit(su, collectionExerciseId);

            mappedSampleUnits.add(mappedSampleUnit);
          });
    }

    return mappedSampleUnits;
  }
}
