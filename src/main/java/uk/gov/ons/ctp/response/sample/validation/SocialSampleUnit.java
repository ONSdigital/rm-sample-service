package uk.gov.ons.ctp.response.sample.validation;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class SocialSampleUnit extends SampleUnitBase {

  public static final Set<String> REQUIRED_ATTRIBUTES =
      ImmutableSet.of("POSTCODE", "COUNTRY", "REFERENCE", "TLA");
  private Map<String, String> attributes;

  public SocialSampleUnit() {
    setSampleUnitType("H");
  }

  public List<String> validate() {
    List<String> invalidColumns = new ArrayList<>();
    if (!getSampleUnitType().equals("H")) {
      invalidColumns.add("sampleUnitType");
    }
    for (String column : REQUIRED_ATTRIBUTES) {
      if (StringUtils.isEmpty(attributes.get(column))) {
        invalidColumns.add(column);
      }
    }
    return invalidColumns;
  }
}
