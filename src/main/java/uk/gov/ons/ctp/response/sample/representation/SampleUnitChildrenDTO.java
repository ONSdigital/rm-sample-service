package uk.gov.ons.ctp.response.sample.representation;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleUnitChildrenDTO {
  private List<SampleUnit> sampleUnitChildren;
}
