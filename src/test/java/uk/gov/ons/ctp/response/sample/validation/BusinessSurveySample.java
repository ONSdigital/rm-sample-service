package uk.gov.ons.ctp.response.sample.validation;

import java.util.List;
import libs.sample.validation.BusinessSampleUnit;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Getter
@NoArgsConstructor
public class BusinessSurveySample extends SurveyBase {

  List<BusinessSampleUnit> sampleUnits;
}
