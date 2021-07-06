package uk.gov.ons.ctp.response.sample.config;

import libs.common.rest.RestUtilityConfig;
import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** App config POJO for Survey service access - host/location and endpoint locations */
@CoverageIgnore
@Data
public class SurveySvc {
  private RestUtilityConfig connectionConfig;
  private String requestClassifierTypesListPath;
  private String requestClassifierTypesPath;
  private String surveyDetailPath;
  private String surveyRefPath;
}
