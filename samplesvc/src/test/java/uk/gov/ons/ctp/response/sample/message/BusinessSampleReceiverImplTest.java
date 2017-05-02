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

import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.message.impl.BusinessSampleReceiverImpl;
import uk.gov.ons.ctp.response.sample.service.SampleService;


/**
 * To unit test CaseReceiptReceiverImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class BusinessSampleReceiverImplTest {

	@InjectMocks
	BusinessSampleReceiverImpl receiver;

	@Mock
	private SampleService sampleService;

	@Test
	public void TestProcessSample() throws Exception{
		
		File file = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/business-survey-sample.xml");
    JAXBContext jaxbContext = JAXBContext.newInstance(BusinessSurveySample.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    BusinessSurveySample sample = (BusinessSurveySample) jaxbUnmarshaller.unmarshal(file);
    
    String load = "";
    
    final Message<String> message = MessageBuilder.withPayload(load).setHeader("file_name", file).build();
		
    List<BusinessSampleUnit> samplingUnitList = sample.getSampleUnits().getBusinessSampleUnits();
    
    HashMap<String,Object> map = new HashMap<String,Object>();
    map.put("file_name", file.getAbsolutePath());
    
	  receiver.processSample(sample, map);
	    
	  verify(sampleService, times(1)).processSampleSummary(sample,samplingUnitList);
	  
	}

}
