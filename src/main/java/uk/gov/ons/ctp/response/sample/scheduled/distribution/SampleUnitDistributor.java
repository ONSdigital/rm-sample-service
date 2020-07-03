package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import libs.common.error.CTPException;
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

  private boolean hasErrors;

  @Autowired private SampleUnitSender sampleUnitSender;

  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitMapper sampleUnitMapper;

  /** Scheduled job for distributing SampleUnits */
  @Transactional(timeout = TRANSACTION_TIMEOUT_SECONDS)
  public void distribute() {
    log.debug("Processing collection exercise jobs triggered by kubernetes");
    collectionExerciseJobRepository.findByJobCompleteIsFalse()
      .stream()
      .forEach(this::processJob);
  }

  private void processJob(CollectionExerciseJob job) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(job.getSampleSummaryId());

    hasErrors = false;

    if (sampleSummary.getState() == SampleState.ACTIVE) {
      try (Stream<uk.gov.ons.ctp.response.sample.domain.model.SampleUnit> sampleUnits =
          sampleUnitRepository.findBySampleSummaryFKAndState(
              sampleSummary.getSampleSummaryPK(), SampleUnitState.PERSISTED)) {
        sampleUnits.forEach(
            su -> {
              SampleUnit mappedSampleUnit =
                  sampleUnitMapper.mapSampleUnit(su, job.getCollectionExerciseId().toString());

              try {
                sampleUnitSender.sendSampleUnit(mappedSampleUnit);
              } catch (CTPException e) {
                hasErrors = true;
                log.error(
                    "Failed to send a sample unit to queue and update state",
                    kv("sample_unit_id", mappedSampleUnit.getId()),
                    "exception",
                    e);
              }
            });
      }
    }

    if (!hasErrors) {
      job.setJobComplete(true);
      collectionExerciseJobRepository.saveAndFlush(job);
    }
  }
}
