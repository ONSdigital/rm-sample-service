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
@Table(name = "collectionexercisejob", schema = "sample")
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
  @Column(name = "collectionexercisejobpk")
  private Integer collectionExerciseJobPK;

  @Column(name = "collectionexerciseid")
  private UUID collectionExerciseId;

  @Column(name = "surveyref")
  private String surveyRef;

  @Column(name = "exercisedatetime")
  private Timestamp exerciseDateTime;

  @Column(name = "createddatetime")
  private Timestamp createdDateTime;

  @Column(name = "samplesummaryid")
  private UUID sampleSummaryId;

  @Column(name = "jobcomplete")
  private boolean jobComplete;
}
