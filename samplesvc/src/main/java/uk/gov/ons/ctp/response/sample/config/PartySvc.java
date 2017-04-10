package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;
import uk.gov.ons.ctp.common.rest.RestClientConfig;

/**
*
* Application config bean for the connection details to the party Service.
*
*/

@Data
public class PartySvc {
  private RestClientConfig connectionConfig;
  private String scheme;
  private String host;
  private String port;
  private String samplePath;
}
