package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import libs.common.error.CTPException;
import libs.common.error.CTPException.Fault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;

/** Accept feedback from handlers */
@Service
public class CollectionExerciseJobService {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseJobService.class);

  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  public void storeCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob)
      throws CTPException {
    UUID collectionExerciseId = collectionExerciseJob.getCollectionExerciseId();
    if (collectionExerciseJobRepository.findByCollectionExerciseId(collectionExerciseId) == null) {
      collectionExerciseJobRepository.saveAndFlush(collectionExerciseJob);
    } else {
      log.debug(
          "CollectionExerciseId already exists in the collectionexercisejob table",
          kv("collection_exercise_id", collectionExerciseId));
      throw new CTPException(
          Fault.BAD_REQUEST,
          "CollectionExerciseId %s already exists in the collectionexercisejob table",
          collectionExerciseId);
    }
  }
}
