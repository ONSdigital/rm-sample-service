package samplesvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.party.representation.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.definition.*;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class SampleServiceImplTest {

  @Test
  public void testRemarshalCensus() throws Exception {
    File file = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/census-survey-sample.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(SurveyBase.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    CensusSurveySample sample = (CensusSurveySample) jaxbUnmarshaller.unmarshal(file);
    for (CensusSampleUnit unit : sample.getSampleUnits().getCensusSampleUnits()) {
      Party p = PartyUtil.convertToPartyDTO(unit);
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
      Party p = PartyUtil.convertToPartyDTO(unit);
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writeValueAsString(p));
    }
  }

  @Test
  public void testRemarshalSocial() throws Exception {
    File file = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/social-survey-sample.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(SocialSurveySample.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    SocialSurveySample sample = (SocialSurveySample) jaxbUnmarshaller.unmarshal(file);
    for (SocialSampleUnit unit : sample.getSampleUnits().getSocialSampleUnits()) {
      Party p = PartyUtil.convertToPartyDTO(unit);
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writeValueAsString(p));
    }
  }

}
