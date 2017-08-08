package uk.gov.ons.ctp.response.sample.representation;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)

public class CollectionExerciseJobCreationRequestDTO {

  @NotNull
  @ApiModelProperty(required = true)
  private UUID collectionExerciseId;

  @NotNull
  @ApiModelProperty(required = true)
  private String surveyRef;

  @NotNull
  @ApiModelProperty(required = true)
  private Date exerciseDateTime;

}
