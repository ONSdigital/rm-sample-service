package uk.gov.ons.ctp.response.sample.message;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import uk.gov.ons.ctp.common.xml.ValidatingXmlUnmarshaller;
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSurveySample;
import uk.gov.ons.ctp.response.sample.message.impl.CensusSampleReceiverImpl;
import uk.gov.ons.ctp.response.sample.service.SampleService;


/**
 * To unit test CaseReceiptReceiverImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class CensusSampleReceiverImplTest {


  @InjectMocks
  CensusSampleReceiverImpl receiver;

  @Mock
  private SampleService sampleService;

  @Test
  public void TestProcessSample() throws Exception{
		String xmlFileLocation = "src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/census-survey-sample.xml";

	  ValidatingXmlUnmarshaller<CensusSurveySample> unmarshaller = new ValidatingXmlUnmarshaller<CensusSurveySample>(
	      "xsd/inbound",
	      "census-survey-sample.xsd",
	      CensusSurveySample.class);
	  CensusSurveySample sample = unmarshaller.unmarshal(xmlFileLocation);
    List<CensusSampleUnit> samplingUnitList = sample.getSampleUnits().getCensusSampleUnits();
    
		File file = new File(xmlFileLocation);
    HashMap<String,Object> map = new HashMap<String,Object>();
    map.put("file_name", file.getAbsolutePath());
    
    receiver.processSample(sample, map);
      
    verify(sampleService, times(1)).processSampleSummary(sample,samplingUnitList);
    
  }

}