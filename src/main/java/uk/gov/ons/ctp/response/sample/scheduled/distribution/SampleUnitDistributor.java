package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import libs.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Distributes SampleUnits to Collex when requested via job. Retries failures until successful */
@Component
public class SampleUnitDistributor {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitDistributor.class);

  private static final int TRANSACTION_TIMEOUT_SECONDS = 3600;

  @Autowired private SampleUnitSender sampleUnitSender;

  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitMapper sampleUnitMapper;

  /** Scheduled job for distributing SampleUnits */
  @Transactional(timeout = TRANSACTION_TIMEOUT_SECONDS)
  public void distribute() {
    log.debug("Collection exercise job distribution has been triggered.");
    collectionExerciseJobRepository.findByJobCompleteIsFalse()
      .stream()
      .forEach(this::processJob);
  }

  private void processJob(CollectionExerciseJob job) {
    List<SampleUnit> invalidSamples = Optional.of(sampleSummaryRepository.findById(job.getSampleSummaryId()))
      .filter(sampleSummary -> sampleSummary.getState() == SampleState.ACTIVE)
      .map(sampleSummary -> sampleUnitRepository.findBySampleSummaryFKAndState(
        sampleSummary.getSampleSummaryPK(), SampleUnitState.PERSISTED))
      .orElseGet(Stream::empty)
      .map(sampleUnit -> sampleUnitMapper.mapSampleUnit(sampleUnit, job.getCollectionExerciseId().toString()))
      .filter(attemptSamplePublish())
      .collect(Collectors.toList());
      
    if (invalidSamples.isEmpty()) {
      log.info("All samples have been published successfully", kv("CollectionExerciseJob", job));
      job.setJobComplete(true);
      collectionExerciseJobRepository.saveAndFlush(job);
      return;
    }
    log.warn("Some samples have failed transition for collection exericse Job", kv("CollectionExerciseJob", job),
      kv("Samples", invalidSamples));
  }

  /**
   * Attempt to publish a SampleUnit. If it fails then return true, otherwise false.
   * This will mimick the previous functionality and allow us to obtain a collection of the
   * Sample units that fail for better logging.
   * @return Predicate<SampleUnit>
   */
  private Predicate<SampleUnit> attemptSamplePublish() {
    return mappedSampleUnit -> {
      try {
        sampleUnitSender.sendSampleUnit(mappedSampleUnit);
        return false;
      } catch (CTPException e) {
        log.error(
          "Failed to send a sample unit to queue and update state",
          kv("sample_unit_id", mappedSampleUnit.getId()),
          "exception",
          e);
        return true;
      }
    };
  }
}
