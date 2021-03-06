package uk.gov.ons.ctp.response.sample.domain.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/** Domain model object. */
@CoverageIgnore
@Entity
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(name = "sampleattributes", schema = "sample")
public class SampleAttributes implements Serializable {

  @Id
  @Column(name = "sampleunitfk")
  private UUID sampleUnitFK;

  @Column(name = "attributes", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private Map<String, String> attributes;
}
