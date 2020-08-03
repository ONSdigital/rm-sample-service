package uk.gov.ons.ctp.response.sample.endpoint;

import static libs.common.MvcHelper.postJson;
import static libs.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static org.assertj.core.api.Java6Assertions.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import libs.common.FixtureHelper;
import libs.common.error.CTPException;
import libs.common.error.InvalidRequestException;
import libs.common.error.RestExceptionHandler;
import libs.common.jackson.CustomObjectMapper;
import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.BusinessSampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;

public class SampleEndpointUnitTest {
  private static final String SAMPLE_VALIDJSON =
      "{ \"collectionExerciseId\" : \"c6467711-21eb-4e78-804c-1db8392f93fb\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"2012-12-13T12:12:12.000Z\", \"sampleSummaryUUIDList\" : [\"c6467711-21eb-4e78-804c-1db8392f93fb\"] }";
  private static final String SAMPLE_INVALIDJSON =
      "{ \"collectionExerciseId\" : \"c6467711-21eb-4e78-804c-1db8393f93fb\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"201.000+0000\" }";

  @InjectMocks private SampleEndpoint sampleEndpoint;

  @Mock private MapperFacade mapperFacade;

  @Mock private SampleService sampleService;

  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc =
        MockMvcBuilders.standaloneSetup(sampleEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();
    FixtureHelper.loadClassFixtures(CollectionExerciseJob[].class);
  }

  @Test
  public void getSampleSummaryValidJSON() throws Exception {
    CollectionExerciseJobCreationRequestDTO cej =
        new ObjectMapper()
            .readValue(SAMPLE_VALIDJSON, CollectionExerciseJobCreationRequestDTO.class);
    when(mapperFacade.map(cej, CollectionExerciseJob.class))
        .thenReturn(new CollectionExerciseJob());
    when(sampleService.initialiseCollectionExerciseJob(any())).thenReturn(4);

    ResultActions actions =
        mockMvc.perform(postJson("/samples/sampleunitrequests", SAMPLE_VALIDJSON));

    actions.andExpect(status().isCreated());
    actions.andExpect(jsonPath("$.sampleUnitsTotal", is(4)));
  }

  @Test
  public void uploadSampleFile() throws Exception {
    // Given
    String type = "B";
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt".getBytes());
    SampleSummary summary = new SampleSummary();
    summary.setId(UUID.randomUUID());

    // when(this.sampleService.ingest(any(SampleSummary.class), any(MultipartFile.class),
    // anyString())).thenReturn(new SampleSummary());
    when(this.sampleService.createAndSaveSampleSummary()).thenReturn(summary);

    // When
    ResultActions actions =
        mockMvc.perform(fileUpload(String.format("/samples/%s/fileupload", type)).file(file));

    // Then
    actions.andExpect(status().isCreated());
  }

  @Test
  public void notFoundWhenUploadSampleFileWithInvalidType() throws Exception {
    // Given
    String type = "invalid-type";
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt".getBytes());

    // When
    ResultActions actions =
        mockMvc.perform(fileUpload(String.format("/samples/%s/fileupload", type)).file(file));

    // Then
    actions.andExpect(status().isBadRequest());
  }

  @Test
  public void acknowledgeReceiptBadJsonProvidedScenario1() throws Exception {
    ResultActions actions =
        mockMvc.perform(postJson(String.format("/samples/sampleunitrequests"), SAMPLE_INVALIDJSON));

    actions
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code", is(CTPException.Fault.VALIDATION_FAILED.name())))
        .andExpect(jsonPath("$.error.timestamp", isA(String.class)))
        .andExpect(jsonPath("$.error.message", is("Provided json is incorrect.")));
  }

  @Test(expected = InvalidRequestException.class)
  public void verifyBadBindingResultThrowsException() throws Exception {
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.hasErrors()).thenReturn(true);
    sampleEndpoint.createSampleUnitRequest(null, bindingResult);
  }

  @Test
  public void getSampleUnitSizeOneSummary() throws Exception {
    when(sampleService.getSampleSummaryUnitCount(any())).thenReturn(666);

    String url = String.format("/samples/count?sampleSummaryId=%s", UUID.randomUUID().toString());

    ResultActions actions = mockMvc.perform(get(url));

    actions.andExpect(status().isOk());
    actions.andExpect(jsonPath("$.sampleUnitsTotal", is(666)));
  }

  @Test
  public void getSampleUnitSizeTwoSummaries() throws Exception {
    when(sampleService.getSampleSummaryUnitCount(any())).thenReturn(33).thenReturn(66);

    String url =
        String.format(
            "/samples/count?sampleSummaryId=%s&sampleSummaryId=%s",
            UUID.randomUUID().toString(), UUID.randomUUID().toString());

    ResultActions actions = mockMvc.perform(get(url));

    actions.andExpect(status().isOk());
    actions.andExpect(jsonPath("$.sampleUnitsTotal", is(99)));
  }

  @Test
  public void addSingleSample() throws Exception {
    BusinessSampleUnitDTO businessSampleUnitDTO = new BusinessSampleUnitDTO();
    businessSampleUnitDTO.setEntname1("test1");
    String body = marshallToJson(businessSampleUnitDTO);

    when(sampleService.createSampleUnit(any(), any(), any())).thenReturn(new SampleUnit());

    String url = String.format("/samples/%s/sampleunits/", UUID.randomUUID());

    ResultActions actions =
        mockMvc.perform(post(url).contentType("application/json").content(body));
    actions.andExpect(status().isCreated());

    verify(sampleService, times(1)).createSampleUnit(any(), any(), any());
  }

  @Test
  public void addSingleSampleInvalidSampleSummaryReturnsBadRequest() throws Exception {
    when(sampleService.createSampleUnit(any(), any(), any()))
        .thenThrow(new UnknownSampleSummaryException());

    BusinessSampleUnitDTO businessSampleUnitDTO = new BusinessSampleUnitDTO();
    businessSampleUnitDTO.setEntname1("test2");
    String body = marshallToJson(businessSampleUnitDTO);

    String url = String.format("/samples/%s/sampleunits/", UUID.randomUUID());

    ResultActions actions =
        mockMvc.perform(post(url).contentType("application/json").content(body));
    actions.andExpect(status().isBadRequest());

    verify(sampleService, times(1)).createSampleUnit(any(), any(), any());
  }

  private String marshallToJson(BusinessSampleUnitDTO businessSampleUnitDTO) {
    String result = null;
    try {
      result = new ObjectMapper().writeValueAsString(businessSampleUnitDTO);
    } catch (JsonProcessingException exc) {
      fail("Unable to convert DTO to JSON");
    }
    return result;
  }
}
