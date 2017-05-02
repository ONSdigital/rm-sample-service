package uk.gov.ons.ctp.response.sample.utility;

import java.sql.Timestamp;
import java.util.List;

import org.glassfish.hk2.api.Factory;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.service.SampleService;

public class MockSampleServiceFactory implements Factory<SampleService> {

  public static final Integer SAMPLE_SAMPLEID = 5;
  public static final String SAMPLE_SURVEYREF = "survey";
  public static final Timestamp SAMPLE_EFFECTIVESTARTDATETIME = Timestamp.valueOf("2012-12-13 12:12:12");
  public static final String SAMPLE_EFFECTIVESTARTDATETIME_OUTPUT = "2012-12-13T12:12:12.000+0000";
  public static final Timestamp SAMPLE_EFFECTIVEENDDATETIME = Timestamp.valueOf("2013-12-13 12:12:12");
  public static final String SAMPLE_EFFECTIVEENDDATETIME_OUTPUT = "2013-12-13T12:12:12.000+0000";
  public static final SampleState SAMPLE_STATE = SampleState.ACTIVE;
  public static final Timestamp SAMPLE_INGESTDATETIME = Timestamp.valueOf("2012-12-18 12:12:12");
  public static final String SAMPLE_INGESTDATETIME_OUTPUT = "2012-12-18T12:12:12.000+0000";

  public static final Integer SAMPLE_COLLECTIONEXERCISEID = 1;
  public static final Integer SAMPLE_SAMPLEUNITSTOTAL = 4;

  /**
   * provide method
   * 
   * @return mocked service
   */
  @Override
  public SampleService provide() {

    final SampleService mockedService = Mockito.mock(SampleService.class);
    try {

      Mockito.when(mockedService.activateSampleSummaryState(SAMPLE_SAMPLEID)).thenAnswer(new Answer<SampleSummary>() {
        public SampleSummary answer(final InvocationOnMock invocation) throws Throwable {

          return new SampleSummary(SAMPLE_SAMPLEID, SAMPLE_SURVEYREF, SAMPLE_EFFECTIVESTARTDATETIME,
              SAMPLE_EFFECTIVEENDDATETIME, SAMPLE_STATE, SAMPLE_INGESTDATETIME);
        }
      });

      List<CollectionExerciseJobCreationRequestDTO> cej = FixtureHelper
          .loadClassFixtures(CollectionExerciseJobCreationRequestDTO[].class);

      Mockito.when(mockedService.initialiseCollectionExerciseJob(cej.get(0))).thenAnswer(new Answer<Integer>() {

        public Integer answer(final InvocationOnMock invocation) throws Throwable {
          return SAMPLE_SAMPLEUNITSTOTAL;
        }

      });

    } catch (Throwable t) {
      throw new RuntimeException(t);
    }

    return mockedService;
  }

  /**
   * dispose method
   * 
   * @param t service to dispose
   */
  @Override
  public void dispose(final SampleService t) {
  }

}
