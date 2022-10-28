package uk.gov.ons.ctp.response.sample.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleSummaryLoadingStatus {
  private boolean areAllSampleUnitsLoaded;
  private Integer expectedTotal;
  private Integer currentTotal;
}
