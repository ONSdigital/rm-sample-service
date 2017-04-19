package uk.gov.ons.ctp.response.sample.domain.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "collectionexercisejob", schema = "sample")
public class CollectionExerciseJob implements Serializable {

  private static final long serialVersionUID = 7778360895016862173L;

  @Id
  @Column(name = "collectionexerciseid")
  private Integer collectionExerciseId;

  @Column(name = "surveyref")
  private String surveyRef;

  @Column(name = "exercisedatetime")
  private Timestamp exerciseDateTime;

  @Column(name = "createddatetime")
  private Timestamp createdDateTime;

}
