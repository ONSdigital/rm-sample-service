package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

@Service
public class CollectionExerciseEndService {

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  private static final Logger LOG = LoggerFactory.getLogger(CollectionExerciseEndService.class);

  public void collectionExerciseEnd(UUID collectionExerciseId) {
    int sampleSummaryPK = markSampleSummaryComplete(collectionExerciseId);

    deleteSampleUnits(collectionExerciseId, sampleSummaryPK);
  }

  private void deleteSampleUnits(UUID collectionExerciseId, int sampleSummaryPk) {
    Stream<SampleUnit> sampleUnitStream =
        sampleUnitRepository.findBySampleSummaryFK(sampleSummaryPk);

    sampleUnitStream
        .parallel()
        .forEach(
            sampleUnit -> {
              try {
                sampleUnitRepository.delete(sampleUnit);
                LOG.info(
                    "Deleting sample unit",
                    kv("Collection excercise ID", collectionExerciseId),
                    kv("sampleUnitId", sampleUnit.getId()));
              } catch (RuntimeException ex) {
                LOG.error(
                    "Failed to delete sample unit",
                    kv("sampleUnitId", sampleUnit.getId()),
                    kv("Collection exercise ID", collectionExerciseId),
                    ex);
                throw ex;
              }
            });
  }

  private int markSampleSummaryComplete(UUID collectionExerciseId) {
    Optional<SampleSummary> optionalSampleSummary =
        sampleSummaryRepository.findByCollectionExerciseId(collectionExerciseId);

    if (optionalSampleSummary.isPresent()) {
      SampleSummary sampleSummary = optionalSampleSummary.get();
      sampleSummary.setState(SampleSummaryDTO.SampleState.COMPLETE);
      sampleSummaryRepository.saveAndFlush(sampleSummary);
      LOG.info(
          "Sample summary marked complete",
          kv("Collection excercise ID", collectionExerciseId),
          kv("Sample summary", sampleSummary.getId()));
      return sampleSummary.getSampleSummaryPK();
    } else {
      LOG.error(
          "Unable to find any sample summaries with Collection Exercise ID: {}",
          collectionExerciseId);
    }
    return 0;
  }
}
