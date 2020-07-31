package uk.gov.ons.ctp.response.sample.config;

import libs.common.rest.RestUtilityConfig;
import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** Application config bean for the connection details to the party Service. */
@CoverageIgnore
@Data
public class SampleFileUploader {
  private RestUtilityConfig connectionConfig;
  private String sampleUploadPath;
}
