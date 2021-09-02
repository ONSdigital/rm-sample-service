package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
  public void distribute() throws SampleDistributionException {
    log.debug("Collection exercise job distribution has been triggered.");
    List<CollectionExerciseJob> jobs = collectionExerciseJobRepository.findByJobCompleteIsFalse();
    for (CollectionExerciseJob job : jobs) {
      processJob(job);
    }
  }

  private void processJob(CollectionExerciseJob job) throws SampleDistributionException {
    UUID sampleSummaryId = job.getSampleSummaryId();
    try {
      List<SampleSummary> sampleSummaries = sampleSummaryRepository.findById(sampleSummaryId);
      if (sampleSummaries == null) {
        throw new NoSuchElementException();
      }
      List<SampleUnit> invalidSamples =
          sampleSummaries.parallelStream()
              .filter(sampleSummary -> sampleSummary.getState() == SampleState.ACTIVE)
              .map(
                  sampleSummary ->
                      sampleUnitRepository.findBySampleSummaryFKAndState(
                          sampleSummary.getSampleSummaryPK(), SampleUnitState.PERSISTED))
              .map(
                  sampleUnit ->
                      sampleUnitMapper.mapSampleUnit(
                          sampleUnit, job.getCollectionExerciseId().toString()))
              .filter(publishSample().negate())
              .collect(Collectors.toList());

      if (!invalidSamples.isEmpty()) {
        throw new SampleDistributionException(
            "Some samples have failed transition for collection exericse Job", job, invalidSamples);
      }
      log.info("All samples have been published successfully", kv("CollectionExerciseJob", job));
      job.setJobComplete(true);
      collectionExerciseJobRepository.saveAndFlush(job);
    } catch (NoSuchElementException e) {
      log.error("unable to find sample summary", kv("sampleSummaryId", sampleSummaryId));
      throw new SampleDistributionException(
          "unable to find sample summary", job, new ArrayList<>());
    }
  }

  /**
   * Publish a SampleUnit. If it passes then return true otherwise false. This will mimick the
   * previous functionality and allow us to obtain a collection of the Sample units that fail for
   * better logging.
   *
   * @return Predicate<SampleUnit>
   */
  private Predicate<SampleUnit> publishSample() {
    return mappedSampleUnit -> {
      try {
        sampleUnitSender.sendSampleUnit(mappedSampleUnit);
        return true;
      } catch (CTPException e) {
        log.error(
            "Failed to send a sample unit to queue and update state",
            kv("sample_unit_id", mappedSampleUnit.getId()),
            "exception",
            e);
        return false;
      }
    };
  }
}
