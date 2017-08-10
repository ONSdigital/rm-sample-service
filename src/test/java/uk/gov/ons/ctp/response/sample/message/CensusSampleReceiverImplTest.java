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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
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
    File xmlFileLocation = new File("src/test/resources/uk/gov/ons/ctp/response/sample/service/impl/census-survey-sample.xml");

    JAXBContext jaxbContext = JAXBContext.newInstance(CensusSurveySample.class);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    CensusSurveySample sample = (CensusSurveySample) jaxbUnmarshaller.unmarshal(xmlFileLocation);
    List<CensusSampleUnit> samplingUnitList = sample.getSampleUnits().getCensusSampleUnits();
    
    HashMap<String,Object> map = new HashMap<String,Object>();
    map.put("file_name", xmlFileLocation.getAbsolutePath());
    
    receiver.processSample(sample, map);
      
    verify(sampleService, times(1)).processSampleSummary(sample,samplingUnitList);
    
  }

}