package uk.gov.ons.ctp.response.sample.domain.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "samplesummary", schema = "sample")
public class SampleSummary implements Serializable {

  private static final long serialVersionUID = 7778360895016862176L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "sampleidseq_gen")
  @GenericGenerator(name = "sampleidseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
  parameters = {
      @Parameter(name = "sequence_name", value = "sample.sampleidseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @Column(name = "sampleid")
  private Integer sampleId;

  @Column(name = "surveyref")
  private String surveyRef;
  
  @Column(name = "effectivestartdatetime")
  private Timestamp effectiveStartDateTime;

  @Column(name = "effectiveenddatetime")
  private Timestamp effectiveEndDateTime;

  @Enumerated(EnumType.STRING)
  private SampleSummaryDTO.SampleState state;
  
  @Column(name = "ingestdatetime")
  private Timestamp ingestDateTime;

}
