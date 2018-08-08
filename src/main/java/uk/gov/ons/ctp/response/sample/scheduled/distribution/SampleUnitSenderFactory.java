package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import java.util.List;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

@Component
public abstract class SampleUnitSenderFactory {

  public SampleUnitSender getNewSampleUnitSender(List<SampleUnit> mappedSampleUnits) {
    SampleUnitSender sampleUnitSender = getNewSampleUnitSender();
    sampleUnitSender.setMappedSampleUnits(mappedSampleUnits);
    return sampleUnitSender;
  }

  @Lookup
  protected abstract SampleUnitSender getNewSampleUnitSender();
}
