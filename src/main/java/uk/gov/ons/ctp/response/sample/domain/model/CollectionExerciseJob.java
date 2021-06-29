package uk.gov.ons.ctp.response.sample.domain.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/** Domain model object. */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "collection_exercise_job", schema = "sample")
public class CollectionExerciseJob implements Serializable {

  private static final long serialVersionUID = 7778360895016862173L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "collectionexercisejobseq_gen")
  @GenericGenerator(
      name = "collectionexercisejobseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "sample.collectionexercisejobseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @Column(name = "collection_exercise_job_pk")
  private Integer collectionExerciseJobPK;

  @Column(name = "collection_exercise_id")
  private UUID collectionExerciseId;

  @Column(name = "survey_ref")
  private String surveyRef;

  @Column(name = "exercise_date_time")
  private Timestamp exerciseDateTime;

  @Column(name = "created_date_time")
  private Timestamp createdDateTime;

  @Column(name = "sample_summary_id")
  private UUID sampleSummaryId;

  @Column(name = "job_complete")
  private boolean jobComplete;
}
