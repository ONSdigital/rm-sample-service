package uk.gov.ons.ctp.response.sample.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;

/**
 * Accept feedback from handlers
 */
@Named
public class CollectionExerciseJobImpl implements CollectionExerciseJobService {

  @Inject
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Override
  public void processCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob) {
    
    collectionExerciseJobRepository.saveAndFlush(collectionExerciseJob);
    
  }

}
