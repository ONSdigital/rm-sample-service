package uk.gov.ons.ctp.response.sample.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;


@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {

  @Mock
  private SampleSummaryRepository sampleSummaryRepository;

  @Mock
  private SampleUnitRepository sampleUnitRepository;

  @Mock
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Mock
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent> sampleSvcUnitStateTransitionManager;

  @Mock
  private PartySvcClientService partySvcClient;

  @Mock
  private PartyPublisher partyPublisher;

  @Mock
  private CollectionExerciseJobService collectionExerciseJobService;

  @InjectMocks
  private SampleServiceImpl sampleServiceImpl;

  private List<BusinessSurveySample> surveySample;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    surveySample = FixtureHelper.loadClassFixtures(BusinessSurveySample[].class);

  }

  @Test
  public void verifySampleSummaryCreatedCorrectly() throws Exception {
    SampleSummary sampleSummary = sampleServiceImpl.createSampleSummary(surveySample.get(0));
    assertTrue(sampleSummary.getSurveyRef().equals("abc"));
    assertNotNull(sampleSummary.getIngestDateTime());
    assertTrue(sampleSummary.getEffectiveEndDateTime().getTime() == 1583743600000L);
    assertTrue(sampleSummary.getEffectiveStartDateTime().getTime() == 1483743600000L);
    assertTrue(sampleSummary.getState() == SampleSummaryDTO.SampleState.INIT);
  }
  
  @Test
  public void processSampleSummaryTest() throws Exception {
    BusinessSurveySample businessSample = surveySample.get(0);
    when(sampleSummaryRepository.save(any(SampleSummary.class))).then(returnsFirstArg());
    sampleServiceImpl.processSampleSummary(businessSample, businessSample.getSampleUnits().getBusinessSampleUnits());
    verify(sampleSummaryRepository).save(any(SampleSummary.class));
    verify(sampleUnitRepository).save(any(SampleUnit.class));
    verify(partyPublisher).publish(any(Party.class));
  }
}
