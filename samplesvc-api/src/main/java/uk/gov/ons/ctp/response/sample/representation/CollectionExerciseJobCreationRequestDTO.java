package uk.gov.ons.ctp.response.sample.representation;

import java.sql.Timestamp;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)

public class CollectionExerciseJobCreationRequestDTO {

  @NotNull
  private Integer collectionExerciseId;

  @NotNull
  private String surveyRef;

  @NotNull
  private Timestamp exerciseDateTime;

}
