package uk.gov.ons.ctp.response.sample.domain.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "sampleunit", schema = "sample")
public class SampleUnit implements Serializable {
  
  private static final long serialVersionUID = 7778360895016862172L;
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "sampleunitidseq_gen")
  @GenericGenerator(name = "sampleunitidseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "sample.sampleunitidseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @Column(name = "sampleunitid")
  private Integer sampleUnitId;
  
  @Column(name = "sampleid")
  private Integer sampleId;
  
  @Column(name = "sampleunitref")
  private String sampleUnitRef;

  @Column(name = "sampleunittype")
  private String sampleUnitType;
  
}
