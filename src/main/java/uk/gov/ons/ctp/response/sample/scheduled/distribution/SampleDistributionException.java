package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.List;

import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

public class SampleDistributionException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private CollectionExerciseJob job;
  private List<SampleUnit> sampleUnits;

  public SampleDistributionException(String message, CollectionExerciseJob job, List<SampleUnit> sampleUnits) {
    super(message);
    this.job = job;
    this.sampleUnits = sampleUnits;
  }

  public List<SampleUnit> getSampleUnits() {
    return this.sampleUnits;
  }
  
  public CollectionExerciseJob getCollectionExerciseJob() {
    return this.job;
  }
}
