package uk.gov.ons.ctp.response.sample.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

/** Domain model object. */
@Entity
@Data
@AllArgsConstructor
@CoverageIgnore
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "sample_summary", schema = "sample")
public class SampleSummary implements Serializable {

  private static final long serialVersionUID = 7778360895016862176L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "samplesummaryseq_gen")
  @GenericGenerator(
      name = "samplesummaryseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "sample.samplesummaryseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @Column(name = "sample_summary_pk")
  private Integer sampleSummaryPK;

  @Column(name = "id")
  @Getter
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "state_fk")
  private SampleSummaryDTO.SampleState state;

  @Column(name = "ingest_date_time")
  private Timestamp ingestDateTime;

  @Column(name = "total_sample_units")
  private Integer totalSampleUnits;

  @Column(name = "expected_collection_instruments")
  private Integer expectedCollectionInstruments;

  @Column(name = "notes")
  private String notes;

  @Column(name = "description")
  @Size(max = 250)
  private String description;

  @Column(name = "collection_exercise_id")
  private UUID collectionExerciseId;

  @Column(name = "survey_id")
  private UUID surveyId;

  @Column(name = "mark_for_deletion")
  private boolean markForDeletion;

  @JsonIgnore
  public Integer getSampleSummaryPK() {
    return sampleSummaryPK;
  }
}
