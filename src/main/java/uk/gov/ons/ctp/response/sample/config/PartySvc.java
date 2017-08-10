package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import uk.gov.ons.ctp.common.rest.RestClientConfig;

/**
*
* Application config bean for the connection details to the party Service.
*
*/
@CoverageIgnore
@Data
public class PartySvc {
  private RestClientConfig connectionConfig;
  private String postPartyPath;
}
