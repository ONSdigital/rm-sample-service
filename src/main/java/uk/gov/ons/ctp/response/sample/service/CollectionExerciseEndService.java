package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Optional;
import java.util.UUID;
import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

@Service
public class CollectionExerciseEndService {

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  private static final Logger LOG = LoggerFactory.getLogger(CollectionExerciseEndService.class);

  public void collectionExerciseEnd(UUID collectionExerciseId) throws CTPException {
    int sampleSummaryPK = markSampleSummaryComplete(collectionExerciseId);
    deleteSampleUnits(sampleSummaryPK);
  }

  private void deleteSampleUnits(int sampleSummaryPk) {
    sampleUnitRepository.deleteBySampleSummaryFK(sampleSummaryPk);
    LOG.info("Sample Units deleted", kv("Sample summary PK", sampleSummaryPk));
  }

  private int markSampleSummaryComplete(UUID collectionExerciseId) throws CTPException {
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
          "Unable to find any sample summaries",
          kv("Collection excercise ID", collectionExerciseId));
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          "Sample Summary not found",
          kv("Collection Exercise ID", collectionExerciseId));
    }
  }
}
