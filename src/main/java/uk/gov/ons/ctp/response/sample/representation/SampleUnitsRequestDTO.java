package uk.gov.ons.ctp.response.sample.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleUnitsRequestDTO {

  private Integer sampleUnitsTotal;
}
