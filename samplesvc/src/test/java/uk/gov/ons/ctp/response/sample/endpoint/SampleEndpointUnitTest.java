package uk.gov.ons.ctp.response.sample.endpoint;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;

public class SampleEndpointUnitTest{
  private static final String SAMPLE_VALIDJSON = "{ \"collectionExerciseId\" : \"1\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"2012-12-13T12:12:12.000+0000\" }";
  private static final String SAMPLE_INVALIDJSON1 = "{ \"collectionExerciseId\" : \"2\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"201.000+0000\" }";
  private static final String SAMPLE_INVALIDJSON2 = "{ \"collectionId\" : \"3\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"2012-12-13T12:12:12.000+0000\" }";
  
  
  private static final Integer SAMPLE_ID = 124;
  
  @InjectMocks
  private SampleEndpoint sampleEndpoint;
  
  @Mock
  private SampleService sampleService;
  
  
  private MockMvc mockMvc;
  private List<SampleSummary> sampleSummaryResults;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    this.mockMvc = MockMvcBuilders.standaloneSetup(sampleEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();
    
    this.sampleSummaryResults = FixtureHelper.loadClassFixtures(SampleSummary[].class);
  }
  
  @Test
  public void getSampleSummaryValidJSON() throws Exception{
    //when(sampleService.findSampleSummaryBySampleId(SAMPLE_ID)).thenReturn(sampleSummaryResults.get(0));
     
    ResultActions actions = mockMvc.perform(postJson(String.format("/samples/sampleunitrequests"), SAMPLE_VALIDJSON));
    
    actions.andExpect(status().isCreated());
  }
  

  @Test
  public void acknowledgeReceiptBadJsonProvidedScenario2() throws Exception {
    ResultActions actions = mockMvc.perform(postJson(String.format("/samples/sampleunitrequests"), SAMPLE_INVALIDJSON1));

    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
        .andExpect(jsonPath("$.error.timestamp", isA(String.class)))
        .andExpect(jsonPath("$.error.message", is("Provided json is incorrect.")));
  }
  
}
