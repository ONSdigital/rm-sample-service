package uk.gov.ons.ctp.response.sample.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;

/**
 * The CollectionExerciseJobService interface defines all business behaviours for operations on
 * the CollectionExerciseJob entity model.
 */
public interface CollectionExerciseJobService {

  /**
   * Create and save a CollectionExerciseJob for the relevant SampleUnits
   *
   * @param collectionExerciseJob CollectionExerciseJob to be used.
   */
  void processCollectionExerciseJob(CollectionExerciseJob collectionExerciseJob) throws CTPException;

}
