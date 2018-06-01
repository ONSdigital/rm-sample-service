package uk.gov.ons.ctp.response.sample.domain.model;


import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(name = "sampleattributes", schema = "sample")
public class SampleAttributes {

    @ManyToOne
    @JoinColumn(name = "sampleunitfk", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private SampleUnit sampleUnit;

    @Id
    @Column(name = "sampleunitfk")
    private UUID sampleUnitFK;

    @Column(name = "attributes", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Map<String, String> attributes;

    public SampleAttributes(UUID sampleUnitFK, Map<String, String> attributes) {
        this.setSampleUnitFK(sampleUnitFK);
        this.setAttributes(attributes);
    }
}
