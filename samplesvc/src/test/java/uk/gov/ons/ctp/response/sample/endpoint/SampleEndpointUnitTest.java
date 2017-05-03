package uk.gov.ons.ctp.response.sample.endpoint;

import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_EFFECTIVESTARTDATETIME_OUTPUT;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_EFFECTIVEENDDATETIME_OUTPUT;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_INGESTDATETIME_OUTPUT;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_SAMPLEID;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_STATE;
import static uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory.SAMPLE_SURVEYREF;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import uk.gov.ons.ctp.common.jersey.CTPJerseyTest;
import uk.gov.ons.ctp.response.sample.SampleBeanMapper;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.utility.MockSampleServiceFactory;

public class SampleEndpointUnitTest extends CTPJerseyTest {
  private static final String SAMPLE_VALIDJSON = "{ \"collectionExerciseId\" : \"1\", \"surveyRef\" : \"string123\", \"exerciseDateTime\" : \"2001-12-31T12:00:00.000+00\" }";

  @Override
  public Application configure() {
    return super.init(SampleEndpoint.class,
        new ServiceFactoryPair [] {
            new ServiceFactoryPair(SampleService.class, MockSampleServiceFactory.class)},
        new SampleBeanMapper());
  }
  
  @Test
  public void activateSampleSummary() {
    with("/samples/%s", SAMPLE_SAMPLEID)
        .put(MediaType.APPLICATION_JSON_TYPE, "")
        .assertResponseCodeIs(HttpStatus.OK)
        .assertStringInBody("$.surveyRef", SAMPLE_SURVEYREF)
        .assertStringInBody("$.effectiveStartDateTime", SAMPLE_EFFECTIVESTARTDATETIME_OUTPUT)
        .assertStringInBody("$.effectiveEndDateTime", SAMPLE_EFFECTIVEENDDATETIME_OUTPUT)
        .assertStringInBody("$.ingestDateTime", SAMPLE_INGESTDATETIME_OUTPUT)
        .assertStringInBody("$.state", SAMPLE_STATE.toString())
        .andClose();
  }
  
  @Test
  public void getSampleSummaryValidJSON() {
    with("/samples/sampleunitrequests")
    .post(MediaType.APPLICATION_JSON_TYPE, SAMPLE_VALIDJSON)
    .assertResponseCodeIs(HttpStatus.OK)
    .assertIntegerInBody("$.sampleUnitsTotal", 4)
    .andClose();
  }

}
