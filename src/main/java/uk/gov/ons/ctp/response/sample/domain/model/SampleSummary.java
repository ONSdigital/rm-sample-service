package uk.gov.ons.ctp.response.sample.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@CoverageIgnore
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "samplesummary", schema = "sample")
public class SampleSummary implements Serializable {

  private static final long serialVersionUID = 7778360895016862176L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "samplesummaryseq_gen")
  @GenericGenerator(name = "samplesummaryseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
  parameters = {
      @Parameter(name = "sequence_name", value = "sample.samplesummaryseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @Column(name = "samplesummarypk")
  private Integer sampleSummaryPK;
  
  @Column(name = "id")
  @Getter
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "statefk")
  private SampleSummaryDTO.SampleState state;

  @Column(name = "ingestdatetime")
  private Timestamp ingestDateTime;

  @Column(name = "totalsampleunits")
  private Integer totalSampleUnits;

  @Column(name = "expectedcollectioninstruments")
  private Integer expectedCollectionInstruments;

  @JsonIgnore
  public Integer getSampleSummaryPK() {
    return sampleSummaryPK;
  }

  @Column(name = "notes")
  @Size(max=255)
  private String notes;

  @Column(name = "description")
  @Size(max=250)
  private String description;
}
