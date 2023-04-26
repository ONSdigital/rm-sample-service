package uk.gov.ons.ctp.response.sample.endpoint;

import static libs.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static org.assertj.core.api.Java6Assertions.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.NoSuchElementException;
import java.util.UUID;
import libs.common.error.RestExceptionHandler;
import libs.common.jackson.CustomObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.BusinessSampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;

public class SampleEndpointUnitTest {

  @InjectMocks private SampleEndpoint sampleEndpoint;

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
  public void deleteSampleSummaryNotFound() throws Exception {
    when(sampleService.findSampleSummary(any())).thenReturn(null);

    String url = String.format("/samples/samplesummary/%s", UUID.randomUUID());

    ResultActions actions = mockMvc.perform(delete(url));

    actions.andExpect(status().isNotFound());
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

  @Test
  public void createSampleSummary() throws Exception {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());

    when(sampleService.createAndSaveSampleSummary(any(SampleSummaryDTO.class)))
        .thenReturn(sampleSummary);

    ResultActions actions =
        mockMvc.perform(
            post("/samples/samplesummary")
                .contentType("application/json")
                .content(
                    "{\"expectedCollectionInstruments\":1,\"totalSampleUnits\":5,\"errorCode\":\"None\"}"));
    actions.andExpect(status().isCreated());

    SampleSummaryDTO dto = new SampleSummaryDTO();
    dto.setTotalSampleUnits(5);
    dto.setExpectedCollectionInstruments(1);

    verify(sampleService, times(1)).createAndSaveSampleSummary(dto);
  }

  @Test
  public void checkAllSampleUnitsForSampleSummaryNotFound() throws Exception {
    when(sampleService.sampleSummaryStateCheck(any())).thenThrow(new NoSuchElementException());

    String url =
        String.format(
            "/samples/samplesummary/%s/check-and-transition-sample-summary-status",
            UUID.randomUUID());

    ResultActions actions = mockMvc.perform(get(url));

    actions.andExpect(status().isNotFound());
  }
}
