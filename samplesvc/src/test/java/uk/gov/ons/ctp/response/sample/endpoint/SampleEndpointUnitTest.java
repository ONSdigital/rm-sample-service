package uk.gov.ons.ctp.response.sample.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Matchers.any;
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
import org.mockito.Spy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.SampleBeanMapper;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
//github.com/ONSdigital/rm-sample-service.git
import uk.gov.ons.ctp.response.sample.service.SampleService;

public class SampleEndpointUnitTest{
  private static final String SAMPLE_VALIDJSON = "{ \"collectionExerciseId\" : \"1\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"2012-12-13T12:12:12.000+0000\" }";
  private static final String SAMPLE_INVALIDJSON = "{ \"collectionExerciseId\" : \"2\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"201.000+0000\" }";
  
  @InjectMocks
  private SampleEndpoint sampleEndpoint;
  
  @Mock
  private SampleService sampleService;
  
  @Spy
  private MapperFacade mapperFacade = new SampleBeanMapper();
  
  private MockMvc mockMvc;

  private List<SampleSummary> sampleSummaryResults;

  private List<CollectionExerciseJob> collectionExerciseRequests;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    this.mockMvc = MockMvcBuilders.standaloneSetup(sampleEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();
    
    this.collectionExerciseRequests = FixtureHelper.loadClassFixtures(CollectionExerciseJob[].class);
  }
  
  @Test
  public void getSampleSummaryValidJSON() throws Exception{
    when(sampleService.initialiseCollectionExerciseJob(any())).thenReturn(4);
     
    ResultActions actions = mockMvc.perform(postJson(String.format("/samples/sampleunitrequests"), SAMPLE_VALIDJSON));
    
    actions.andExpect(status().isCreated());
    actions.andExpect(jsonPath("$.sampleUnitsTotal", is(4)));
  }
  

  @Test
  public void acknowledgeReceiptBadJsonProvidedScenario1() throws Exception {
    ResultActions actions = mockMvc.perform(postJson(String.format("/samples/sampleunitrequests"), SAMPLE_INVALIDJSON));

    actions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
        .andExpect(jsonPath("$.error.timestamp", isA(String.class)))
        .andExpect(jsonPath("$.error.message", is("Provided json is incorrect.")));
  }
  
}
