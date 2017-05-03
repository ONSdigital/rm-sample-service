package uk.gov.ons.ctp.response.sample.endpoint;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_EFFECTIVEENDDATETIME_OUTPUT;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_EFFECTIVESTARTDATETIME_OUTPUT;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_INGESTDATETIME_OUTPUT;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_SAMPLEID;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_STATE;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_SURVEYREF;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;

public class SampleEndpointUnitTest{
  private static final String SAMPLE_VALIDJSON = "{ \"collectionExerciseId\" : \"1\", \"surveyRef\" : \"string123\", \"exerciseDateTime\" : \"2001-12-31T12:00:00.000+00\" }";

  private static final Integer SAMPLE_ID = 124;
  
//  @Override
//  public Application configure() {
//    return super.init(SampleEndpoint.class,
//        new ServiceFactoryPair [] {
//            new ServiceFactoryPair(SampleService.class, MockSampleServiceFactory.class)},
//        new SampleBeanMapper());
//  }
//  
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
  public void activateSampleSummary() throws Exception {
    when(sampleService.findSampleSummaryBySampleId(SAMPLE_ID)).thenReturn(sampleSummaryResults.get(0));
    
    ResultActions actions = mockMvc.perform(getJson(String.format("/samples/%s", SAMPLE_SAMPLEID)));

    actions.andExpect(status().isOk());
    actions.andExpect(handler().handlerType(SampleEndpoint.class));
    actions.andExpect(handler().methodName("activateSampleSummary"));
    actions.andExpect(jsonPath("$.surveyRef", is(SAMPLE_SURVEYREF)));
    actions.andExpect(jsonPath("$.effectiveStartDateTime", is(SAMPLE_EFFECTIVESTARTDATETIME_OUTPUT)));
    actions.andExpect(jsonPath("$.effectiveEndDateTime",is( SAMPLE_EFFECTIVEENDDATETIME_OUTPUT)));
    actions.andExpect(jsonPath("$.ingestDateTime", is(SAMPLE_INGESTDATETIME_OUTPUT)));
    actions.andExpect(jsonPath("$.state", is(SAMPLE_STATE.toString())));
      
  }
  
//  @Test
//  public void getSampleSummaryValidJSON() {
//    with("/samples/sampleunitrequests")
//    .post(MediaType.APPLICATION_JSON_TYPE, SAMPLE_VALIDJSON)
//    .assertResponseCodeIs(HttpStatus.OK)
//    .assertIntegerInBody("$.sampleUnitsTotal", 4)
//    .andClose();
//  }

}
