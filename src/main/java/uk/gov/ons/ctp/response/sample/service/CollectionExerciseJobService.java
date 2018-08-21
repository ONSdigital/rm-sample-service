package uk.gov.ons.ctp.response.sample.service;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;

/** Accept feedback from handlers */
@Service
@Slf4j
public class CollectionExerciseJobService {

  @Autowired private CollectionExerciseJobRepository collectionExerciseJobRepository;

  public void storeCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob)
      throws CTPException {
    UUID collectionExerciseId = collectionExerciseJob.getCollectionExerciseId();
    if (collectionExerciseJobRepository.findByCollectionExerciseId(collectionExerciseId) == null) {
      collectionExerciseJobRepository.saveAndFlush(collectionExerciseJob);
    } else {
      log.debug(
          "CollectionExerciseId {} already exists in the collectionexercisejob table",
          collectionExerciseId);
      throw new CTPException(
          Fault.BAD_REQUEST,
          "CollectionExerciseId %s already exists in the collectionexercisejob table",
          collectionExerciseId);
    }
  }
}
