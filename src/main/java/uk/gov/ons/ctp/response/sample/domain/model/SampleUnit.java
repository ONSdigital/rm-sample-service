package uk.gov.ons.ctp.response.sample.domain.model;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/** Domain model object. */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "sample_unit", schema = "sample")
public class SampleUnit implements Serializable {

  private static final long serialVersionUID = 7778360895016862172L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "sampleunitseq_gen")
  @GenericGenerator(
      name = "sampleunitseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "sample.sampleunitseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @Column(name = "sample_unit_pk")
  private Integer sampleUnitPK;

  @Column(name = "id")
  private UUID id;

  @Column(name = "sample_summary_fk")
  private Integer sampleSummaryFK;

  @Column(name = "sample_unit_ref")
  private String sampleUnitRef;

  @Column(name = "sample_unit_type")
  private String sampleUnitType;

  @Column(name = "form_type")
  private String formType;

  @Enumerated(EnumType.STRING)
  @Column(name = "state_fk")
  private SampleUnitDTO.SampleUnitState state;

  @Column(name = "party_id")
  private UUID partyId;

  @Transient private SampleAttributes sampleAttributes;
}
