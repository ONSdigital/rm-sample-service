package samplesvc;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlType;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.party.definition.PartyAttributeMap;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.xml.JaxbAnnotatedTypeUtil;

public class SampleServiceImplTest {

  @Test
  public void testRemarshalCensus() throws Exception {
    File file = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/census-survey-sample.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(SurveyBase.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    CensusSurveySample sample = (CensusSurveySample) jaxbUnmarshaller.unmarshal(file);
    for (CensusSampleUnit unit : sample.getSampleUnits().getCensusSampleUnits()) {
      Party p = PartyUtil.convertToParty(unit);
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writeValueAsString(p));
    }
  }

  @Test
  public void testRemarshalBusiness() throws Exception {
    File file = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/business-survey-sample.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(BusinessSurveySample.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    BusinessSurveySample sample = (BusinessSurveySample) jaxbUnmarshaller.unmarshal(file);
    for (BusinessSampleUnit unit : sample.getSampleUnits().getBusinessSampleUnits()) {
      Party p = PartyUtil.convertToParty(unit);
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writeValueAsString(p));
    }
  }


}
