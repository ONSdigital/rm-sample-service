package uk.gov.ons.ctp.response.sample.xml;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

/** 
 * Util class to manipulate or inspect Jaxb type annotated objects
 *
 */
public class JaxbAnnotatedTypeUtil {

  /**
   * Takes the given object and extracts using reflection the @XmlType annotated
   * properties, returning a map where the keys are the property names and the
   * values are the property values.
   * 
   * @param xmlObject the object to extract properties from
   * @return the map of property names and their values
   * @throws Exception Should not happen. Something went really wrong.
   */
  public static Map<String, String> extractXmlProperties(Object xmlObject) throws Exception {
    return extractXmlProperties(xmlObject, xmlObject.getClass());
  }

  /**
   * Navigates up the inheritance chain of an object recursively, harvesting the
   * fields from each class level that have been annotated with @XmlType
   * 
   * @param xmlObject the object to extract properties from
   * @param xmlClass the class from which the Xml declared fields should be
   *          harvested
   * @return the map of property names and their values
   * @throws Exception Should not happen. Something went really wrong.
   */
  private static Map<String, String> extractXmlProperties(Object xmlObject, Class<? extends Object> xmlClass)
      throws Exception {
    Map<String, String> attribBucket = new HashMap<>();
    if (xmlClass != null) {
      XmlType xmlAnnotation = xmlClass.getAnnotation(XmlType.class);
      if (xmlAnnotation != null) {
        String[] attribs = xmlAnnotation.propOrder();

        for (String attrib : attribs) {
          if (!attrib.equals("")) {
            Field field = xmlClass.getDeclaredField(attrib);
            field.setAccessible(true);
            Object obj = field.get(xmlObject);
            if (obj != null && !obj.toString().equals("")) {
              attribBucket.put(attrib, obj.toString());
            }
          }
        }
      }
      attribBucket.putAll(extractXmlProperties(xmlObject, xmlClass.getSuperclass()));
    }
    return attribBucket;
  }
}
