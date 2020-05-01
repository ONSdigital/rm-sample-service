//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.04.29 at 11:55:48 AM BST
//

package libs.party.definition;

import com.kscs.util.jaxb.Buildable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for SampleLinkCreationRequestDTO complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SampleLinkCreationRequestDTO"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="collectionExerciseId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "SampleLinkCreationRequestDTO",
    propOrder = {"collectionExerciseId"})
@XmlRootElement(name = "sampleLinkCreationRequestDTO")
public class SampleLinkCreationRequestDTO {

  @XmlElement(required = true)
  protected String collectionExerciseId;

  /** Default no-arg constructor */
  public SampleLinkCreationRequestDTO() {
    super();
  }

  /** Fully-initialising value constructor */
  public SampleLinkCreationRequestDTO(final String collectionExerciseId) {
    this.collectionExerciseId = collectionExerciseId;
  }

  /**
   * Gets the value of the collectionExerciseId property.
   *
   * @return possible object is {@link String }
   */
  public String getCollectionExerciseId() {
    return collectionExerciseId;
  }

  /**
   * Sets the value of the collectionExerciseId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setCollectionExerciseId(String value) {
    this.collectionExerciseId = value;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(final Builder<_B> _other) {
    _other.collectionExerciseId = this.collectionExerciseId;
  }

  public <_B> Builder<_B> newCopyBuilder(final _B _parentBuilder) {
    return new Builder<_B>(_parentBuilder, this, true);
  }

  public Builder<Void> newCopyBuilder() {
    return newCopyBuilder(null);
  }

  public static Builder<Void> builder() {
    return new Builder<Void>(null, null, false);
  }

  public static <_B> Builder<_B> copyOf(final SampleLinkCreationRequestDTO _other) {
    final Builder<_B> _newBuilder = new Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder);
    return _newBuilder;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(
      final Builder<_B> _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PropertyTree collectionExerciseIdPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("collectionExerciseId"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (collectionExerciseIdPropertyTree != null)
        : ((collectionExerciseIdPropertyTree == null)
            || (!collectionExerciseIdPropertyTree.isLeaf())))) {
      _other.collectionExerciseId = this.collectionExerciseId;
    }
  }

  public <_B> Builder<_B> newCopyBuilder(
      final _B _parentBuilder,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    return new Builder<_B>(_parentBuilder, this, true, _propertyTree, _propertyTreeUse);
  }

  public Builder<Void> newCopyBuilder(
      final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
    return newCopyBuilder(null, _propertyTree, _propertyTreeUse);
  }

  public static <_B> Builder<_B> copyOf(
      final SampleLinkCreationRequestDTO _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final Builder<_B> _newBuilder = new Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder, _propertyTree, _propertyTreeUse);
    return _newBuilder;
  }

  public static Builder<Void> copyExcept(
      final SampleLinkCreationRequestDTO _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.EXCLUDE);
  }

  public static Builder<Void> copyOnly(
      final SampleLinkCreationRequestDTO _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.INCLUDE);
  }

  public boolean equals(Object object) {
    if ((object == null) || (this.getClass() != object.getClass())) {
      return false;
    }
    if (this == object) {
      return true;
    }
    final SampleLinkCreationRequestDTO that = ((SampleLinkCreationRequestDTO) object);
    {
      String leftCollectionExerciseId;
      leftCollectionExerciseId = this.getCollectionExerciseId();
      String rightCollectionExerciseId;
      rightCollectionExerciseId = that.getCollectionExerciseId();
      if (this.collectionExerciseId != null) {
        if (that.collectionExerciseId != null) {
          if (!leftCollectionExerciseId.equals(rightCollectionExerciseId)) {
            return false;
          }
        } else {
          return false;
        }
      } else {
        if (that.collectionExerciseId != null) {
          return false;
        }
      }
    }
    return true;
  }

  public int hashCode() {
    int currentHashCode = 1;
    {
      currentHashCode = (currentHashCode * 31);
      String theCollectionExerciseId;
      theCollectionExerciseId = this.getCollectionExerciseId();
      if (this.collectionExerciseId != null) {
        currentHashCode += theCollectionExerciseId.hashCode();
      }
    }
    return currentHashCode;
  }

  public static class Builder<_B> implements Buildable {

    protected final _B _parentBuilder;
    protected final SampleLinkCreationRequestDTO _storedValue;
    private String collectionExerciseId;

    public Builder(
        final _B _parentBuilder, final SampleLinkCreationRequestDTO _other, final boolean _copy) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          this.collectionExerciseId = _other.collectionExerciseId;
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public Builder(
        final _B _parentBuilder,
        final SampleLinkCreationRequestDTO _other,
        final boolean _copy,
        final PropertyTree _propertyTree,
        final PropertyTreeUse _propertyTreeUse) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          final PropertyTree collectionExerciseIdPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("collectionExerciseId"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (collectionExerciseIdPropertyTree != null)
              : ((collectionExerciseIdPropertyTree == null)
                  || (!collectionExerciseIdPropertyTree.isLeaf())))) {
            this.collectionExerciseId = _other.collectionExerciseId;
          }
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public _B end() {
      return this._parentBuilder;
    }

    protected <_P extends SampleLinkCreationRequestDTO> _P init(final _P _product) {
      _product.collectionExerciseId = this.collectionExerciseId;
      return _product;
    }

    /**
     * Sets the new value of "collectionExerciseId" (any previous value will be replaced)
     *
     * @param collectionExerciseId New value of the "collectionExerciseId" property.
     */
    public Builder<_B> withCollectionExerciseId(final String collectionExerciseId) {
      this.collectionExerciseId = collectionExerciseId;
      return this;
    }

    @Override
    public SampleLinkCreationRequestDTO build() {
      if (_storedValue == null) {
        return this.init(new SampleLinkCreationRequestDTO());
      } else {
        return ((SampleLinkCreationRequestDTO) _storedValue);
      }
    }
  }

  public static class Select extends Selector<Select, Void> {

    Select() {
      super(null, null, null);
    }

    public static Select _root() {
      return new Select();
    }
  }

  public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?>, TParent>
      extends com.kscs.util.jaxb.Selector<TRoot, TParent> {

    private com.kscs.util.jaxb.Selector<TRoot, Selector<TRoot, TParent>> collectionExerciseId =
        null;

    public Selector(final TRoot root, final TParent parent, final String propertyName) {
      super(root, parent, propertyName);
    }

    @Override
    public Map<String, PropertyTree> buildChildren() {
      final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
      products.putAll(super.buildChildren());
      if (this.collectionExerciseId != null) {
        products.put("collectionExerciseId", this.collectionExerciseId.init());
      }
      return products;
    }

    public com.kscs.util.jaxb.Selector<TRoot, Selector<TRoot, TParent>> collectionExerciseId() {
      return ((this.collectionExerciseId == null)
          ? this.collectionExerciseId =
              new com.kscs.util.jaxb.Selector<TRoot, Selector<TRoot, TParent>>(
                  this._root, this, "collectionExerciseId")
          : this.collectionExerciseId);
    }
  }
}
