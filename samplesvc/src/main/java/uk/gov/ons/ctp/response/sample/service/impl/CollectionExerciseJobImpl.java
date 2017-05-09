package uk.gov.ons.ctp.response.sample.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;

/**
 * Accept feedback from handlers
 */
@Service
@Slf4j
public class CollectionExerciseJobImpl implements CollectionExerciseJobService {

  @Autowired
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Override
  public void processCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob) throws CTPException {

    int collectionExerciseId = collectionExerciseJob.getCollectionExerciseId();

    if (collectionExerciseJobRepository.findOne(collectionExerciseId) == null) {
      collectionExerciseJobRepository.saveAndFlush(collectionExerciseJob);
    } else {
      log.debug("CollectionExerciseId {} already exists in the collectionexercisejob table", collectionExerciseId);
      throw new CTPException(Fault.BAD_REQUEST,
          "CollectionExerciseId %s already exists in the collectionexercisejob table", collectionExerciseId);
    }

  }

}
