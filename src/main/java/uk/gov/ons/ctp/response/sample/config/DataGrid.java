package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** Config POJO for action plan exec params */
@CoverageIgnore
@Data
public class DataGrid {
  private String address;
  private String password;
  private Integer lockTimeToWaitSeconds;
  private Integer lockTimeToLiveSeconds;
}
