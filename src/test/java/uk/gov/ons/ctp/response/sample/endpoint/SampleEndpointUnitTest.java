package uk.gov.ons.ctp.response.sample.endpoint;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import ma.glasnost.orika.MapperFacade;
import org.json.JSONObject;
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
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleAttributesDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.SampleUnitDistributor;

public class SampleEndpointUnitTest {
  private static final String SAMPLE_VALIDJSON =
      "{ \"collectionExerciseId\" : \"c6467711-21eb-4e78-804c-1db8392f93fb\", \"sampleSummaryUUIDList\" : [\"c6467711-21eb-4e78-804c-1db8392f93fb\"] }";
  private static final String SAMPLE_INVALIDJSON =
      "{ \"collectionExerciseId\" : \"c6467711-21eb-4e78-804c-1db8393f93fb\" }";

  @InjectMocks private SampleEndpoint sampleEndpoint;

  @Mock private MapperFacade mapperFacade;

  @Mock private SampleService sampleService;

  @Mock private SampleUnitDistributor sampleUnitDistributor;

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
  public void requestSampleUnits() throws Exception {
    when(sampleService.findSampleSummary(any()))
        .thenReturn(SampleSummary.builder().totalSampleUnits(4).build());

    ResultActions actions =
        mockMvc.perform(postJson("/samples/sampleunitrequests", SAMPLE_VALIDJSON));

    actions.andExpect(status().isOk());
    actions.andExpect(jsonPath("$.sampleUnitsTotal", is(4)));
  }

  @Test
  public void requestSampleUnitsAllowsAdditionalFields() throws Exception {
    when(sampleService.findSampleSummary(any()))
        .thenReturn(SampleSummary.builder().totalSampleUnits(4).build());
    JSONObject jsonObject = new JSONObject(SAMPLE_VALIDJSON);
    jsonObject.put("I don't belong here", "");

    ResultActions actions =
        mockMvc.perform(postJson("/samples/sampleunitrequests", jsonObject.toString()));

    actions.andExpect(status().isOk());
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
        .andExpect(jsonPath("$.error.message", is("Provided json fails validation.")));
  }

  @Test(expected = InvalidRequestException.class)
  public void verifyBadBindingResultThrowsException() throws Exception {
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.hasErrors()).thenReturn(true);
    sampleEndpoint.publishSampleUnits(null, bindingResult);
  }

  @Test
  public void ensureAttributesReturnedById() throws Exception {

    UUID id = UUID.randomUUID();
    SampleUnit sampleUnit = new SampleUnit();
    SampleAttributes sampleAttribs = new SampleAttributes();
    SampleAttributesDTO sampleAttributesDTO = new SampleAttributesDTO();
    Map<String, String> attribs = new HashMap<>();

    attribs.put("Reference", "LMS0001");
    sampleAttribs.setSampleUnitFK(id);
    sampleAttribs.setAttributes(attribs);

    sampleAttributesDTO.setId(id);
    sampleAttributesDTO.setAttributes(attribs);

    sampleUnit.setId(id);
    sampleUnit.setSampleAttributes(sampleAttribs);

    when(sampleService.findSampleUnitBySampleUnitId(any())).thenReturn(sampleUnit);
    when(sampleService.findSampleAttributes(any())).thenReturn(sampleAttribs);
    when(mapperFacade.map(sampleAttribs, SampleAttributesDTO.class))
        .thenReturn(sampleAttributesDTO);

    ResultActions getAttribs =
        mockMvc.perform(get(String.format("/samples/%s/attributes", id.toString())));

    getAttribs.andExpect(status().isOk());

    assertThat(sampleAttributesDTO.getId()).isEqualTo(id);
  }
}
