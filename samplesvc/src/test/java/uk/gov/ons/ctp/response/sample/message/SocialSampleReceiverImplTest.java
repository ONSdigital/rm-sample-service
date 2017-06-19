package uk.gov.ons.ctp.response.sample.message;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.ons.ctp.common.xml.ValidatingXmlUnmarshaller;
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SocialSurveySample;
import uk.gov.ons.ctp.response.sample.message.impl.SocialSampleReceiverImpl;
import uk.gov.ons.ctp.response.sample.service.SampleService;


/**
 * To unit test CaseReceiptReceiverImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class SocialSampleReceiverImplTest {


  @InjectMocks
  SocialSampleReceiverImpl receiver;

  @Mock
  private SampleService sampleService;

  @Test
  public void TestProcessSample() throws Exception{
    
		String xmlFileLocation = "src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/social-survey-sample.xml";

	  ValidatingXmlUnmarshaller<SocialSurveySample> unmarshaller = new ValidatingXmlUnmarshaller<SocialSurveySample>(
              "samplesvc/xsd/inbound",
	      "social-survey-sample.xsd",
	      SocialSurveySample.class);
	  SocialSurveySample sample = unmarshaller.unmarshal(xmlFileLocation);
    List<SocialSampleUnit> samplingUnitList = sample.getSampleUnits().getSocialSampleUnits();
    
		File file = new File(xmlFileLocation);
    HashMap<String,Object> map = new HashMap<String,Object>();
    map.put("file_name", file.getAbsolutePath());
    
    receiver.processSample(sample, map);
      
    verify(sampleService, times(1)).processSampleSummary(sample,samplingUnitList);
    
  }

}