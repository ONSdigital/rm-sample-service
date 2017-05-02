package uk.gov.ons.ctp.response.sample.representation;

import java.util.Date;

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

public class CollectionExerciseJobDTO {

  private Integer collectionExerciseId;

  private String surveyRef;

  private Date exerciseDateTime;

  private Date createdDateTime;
}
