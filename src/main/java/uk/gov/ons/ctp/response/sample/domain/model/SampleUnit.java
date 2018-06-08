package uk.gov.ons.ctp.response.sample.domain.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Parameter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "sampleunit", schema = "sample")
public class SampleUnit implements Serializable {

  private static final long serialVersionUID = 7778360895016862172L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "sampleunitseq_gen")
  @GenericGenerator(name = "sampleunitseq_gen", strategy =
  "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "sample.sampleunitseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @Column(name = "sampleunitpk")
  private Integer sampleUnitPK;
  
  @Column(name = "id")
  @NaturalId
  private UUID id;

  @Column(name = "samplesummaryfk")
  private Integer sampleSummaryFK;

  @Column(name = "sampleunitref")
  private String sampleUnitRef;

  @Column(name = "sampleunittype")
  private String sampleUnitType;

  @Column(name = "formtype")
  private String formType;

  @Enumerated(EnumType.STRING)
  @Column(name = "statefk")
  private SampleUnitDTO.SampleUnitState state;

  @Transient
  private SampleAttributes sampleAttributes;
}
