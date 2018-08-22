package uk.gov.ons.ctp.response.sample.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
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
          "CollectionExerciseId {} already exists in the collectionexercisejob table",
          collectionExerciseId);
      throw new CTPException(
          Fault.BAD_REQUEST,
          "CollectionExerciseId %s already exists in the collectionexercisejob table",
          collectionExerciseId);
    }
  }
}
