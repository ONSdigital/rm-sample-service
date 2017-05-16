package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;

/**
 * Config POJO for action plan exec params
 *
 */
@Data
public class DataGrid {
  private String address;
  private String password;
  private Integer listTimeToWaitSeconds;
  private Integer listTimeToLiveSeconds;
  private Integer lockTimeToLiveSeconds;
}
