package uk.gov.ons.ctp.response.sample.representation;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleSummaryStatusDTO {
  private UUID collectionExerciseId;

  private Boolean successful;

  public enum Event {
    DISTRIBUTED,
    ENRICHED
  }
  private Event event;
}
