package uk.gov.ons.ctp.response.sample.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleUnitChildrenDTO {
  protected List<SampleUnit> sampleUnitChildren;
}
