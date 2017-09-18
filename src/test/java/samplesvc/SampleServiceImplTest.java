package samplesvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Tests remarshalling for Census, Business and Social
 */
public class SampleServiceImplTest {

  /**
   * Test Remarshalling for Census
   * @throws Exception if Remarshalling is unsuccessful
   */
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

  /**
   * Test Remarshalling for Business
   * @throws Exception if Remarshalling is unsuccessful
   */
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

  /**
   * Test Remarshalling for Social
   * @throws Exception if Remarshalling is unsuccessful
   */
  @Test
  public void testRemarshalSocial() throws Exception {
    File file = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/social-survey-sample.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(SocialSurveySample.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    SocialSurveySample sample = (SocialSurveySample) jaxbUnmarshaller.unmarshal(file);
    for (SocialSampleUnit unit : sample.getSampleUnits().getSocialSampleUnits()) {
      Party p = PartyUtil.convertToParty(unit);
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writeValueAsString(p));
    }
  }
}
