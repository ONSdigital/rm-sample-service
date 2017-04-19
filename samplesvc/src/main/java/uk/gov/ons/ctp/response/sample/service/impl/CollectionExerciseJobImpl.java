package uk.gov.ons.ctp.response.sample.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;

/**
 * Accept feedback from handlers
 */
@Named
@Slf4j
public class CollectionExerciseJobImpl implements CollectionExerciseJobService {

  @Inject
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Override
  public void processCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob) {
    
    int collectionExerciseId = collectionExerciseJob.getCollectionExerciseId();
    
    if(collectionExerciseJobRepository.findOne(collectionExerciseId) == null) {
      collectionExerciseJobRepository.saveAndFlush(collectionExerciseJob);
    } else {
      log.debug("CollectionExerciseId {} already exists in the collectionexercisejob table", collectionExerciseId);
    }
    
  }

}
