package uk.gov.ons.ctp.response.sample.validation;

import java.util.function.Function;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

/**
 * Classifier Types used to search the Collection Instrument Service to return the appropriate
 * collection instrument for a sample unit. The classifiers used in the Collection Instrument
 * Service Search.
 *
 * <p>Note. These classifier types need to match the classifier types returned from the Survey
 * service for the Collection Instrument Selector Type, with an appropriate lambda expression to
 * obtain the value.
 */
public enum CollectionInstrumentClassifierTypes implements Function<SampleUnit, String> {
  RU_REF(SampleUnit::getSampleUnitRef),
  FORM_TYPE(SampleUnit::getFormType);

  private final Function<SampleUnit, String> func;

  /**
   * Create an instance of the enum.
   *
   * @param lambda expression to be applied to obtain value for classifier.
   */
  CollectionInstrumentClassifierTypes(Function<SampleUnit, String> lambda) {
    this.func = lambda;
  }

  @Override
  public String apply(SampleUnit unit) {
    return func.apply(unit);
  }
}
