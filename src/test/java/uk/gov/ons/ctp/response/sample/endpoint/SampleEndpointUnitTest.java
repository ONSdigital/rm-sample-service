package uk.gov.ons.ctp.response.sample.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.InvalidRequestException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

public class SampleEndpointUnitTest {
    private static final String SAMPLE_VALIDJSON = "{ \"collectionExerciseId\" : \"c6467711-21eb-4e78-804c-1db8392f93fb\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"2012-12-13T12:12:12.000+0000\", \"sampleSummaryUUIDList\" : [\"c6467711-21eb-4e78-804c-1db8392f93fb\"] }";
    private static final String SAMPLE_INVALIDJSON = "{ \"collectionExerciseId\" : \"c6467711-21eb-4e78-804c-1db8393f93fb\", \"surveyRef\" : \"str1234\", \"exerciseDateTime\" : \"201.000+0000\" }";

    @InjectMocks
    private SampleEndpoint sampleEndpoint;

    @Mock
    private MapperFacade mapperFacade;

    @Mock
    private SampleService sampleService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(sampleEndpoint)
                .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
                .build();
        FixtureHelper.loadClassFixtures(CollectionExerciseJob[].class);
    }

    @Test
    public void getSampleSummaryValidJSON() throws Exception {
        CollectionExerciseJobCreationRequestDTO cej = new ObjectMapper().readValue(SAMPLE_VALIDJSON, CollectionExerciseJobCreationRequestDTO.class);
        when(mapperFacade.map(cej, CollectionExerciseJob.class)).thenReturn(new CollectionExerciseJob());
        when(sampleService.initialiseCollectionExerciseJob(any())).thenReturn(4);

        ResultActions actions = mockMvc.perform(postJson("/samples/sampleunitrequests", SAMPLE_VALIDJSON));

        actions.andExpect(status().isCreated());
        actions.andExpect(jsonPath("$.sampleUnitsTotal", is(4)));
    }

    @Test
    public void uploadSampleFile() throws Exception {
        // Given
        String type = "B";
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt".getBytes());

        // When
        ResultActions actions = mockMvc.perform(fileUpload(String.format("/samples/%s/fileupload?collectionExerciseId=test", type)).file(file));

        // Then
        actions.andExpect(status().isCreated());
    }

    @Test
    public void notFoundWhenUploadSampleFileWithInvalidType() throws Exception {
        // Given
        String type = "invalid-type";
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt".getBytes());

        // When
        ResultActions actions = mockMvc.perform(fileUpload(String.format("/samples/%s/fileupload?collectionExerciseId=test", type)).file(file));

        // Then
        actions.andExpect(status().isBadRequest());
    }

    @Test
    public void acknowledgeReceiptBadJsonProvidedScenario1() throws Exception {
        ResultActions actions = mockMvc.perform(postJson(String.format("/samples/sampleunitrequests"), SAMPLE_INVALIDJSON));

        actions.andExpect(status().isBadRequest())
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

}
