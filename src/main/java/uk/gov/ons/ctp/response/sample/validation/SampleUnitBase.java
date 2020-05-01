package uk.gov.ons.ctp.response.sample.validation;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SampleUnitBase {

  String sampleUnitRef;

  String sampleUnitType;

  String formType;

  UUID sampleUnitId = UUID.randomUUID();
}
