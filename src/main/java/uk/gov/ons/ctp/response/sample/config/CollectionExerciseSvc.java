package uk.gov.ons.ctp.response.sample.config;

import libs.common.rest.RestUtilityConfig;
import lombok.Data;

/** App config POJO for CollectionExercise service access */
@Data
public class CollectionExerciseSvc {
  private RestUtilityConfig connectionConfig;
  private String sampleValidatedPath;
  private String sampleDistributedPath;
}
