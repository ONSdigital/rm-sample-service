package uk.gov.ons.ctp.response.sample.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.common.TestHelper.createTestDate;


/**
 * Created by wardlk on 04/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {

    @InjectMocks
    private SampleServiceImpl sampleServiceImpl;

    @Mock
    private SampleSummaryRepository sampleSummaryRepository;

    @Mock
    private RestClient partySvcClient;

    @Mock
    private AppConfig appConfig;

    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private static final int SAMPLE_SUMMARY_PK = 1;

    @Test
    public void verifySampleSummaryCreatedCorrectly() throws Exception {
        List<SurveyBase> surveySample = FixtureHelper.loadClassFixtures(SurveyBase[].class);
        SampleSummary sampleSummary = sampleServiceImpl.createSampleSummary(surveySample.get(0));
        assertTrue(sampleSummary.getSurveyRef().equals("abc"));
        assertNotNull(sampleSummary.getIngestDateTime());
        assertTrue(sampleSummary.getEffectiveEndDateTime().getTime() == 1583743600000L);
        assertTrue(sampleSummary.getEffectiveStartDateTime().getTime() == 1483743600000L);
        assertTrue(sampleSummary.getState() == SampleSummaryDTO.SampleState.INIT);
    }

    @Test
    public void verifySampleUnitsCreatedCorrectly() throws Exception {
        List<SurveyBase> surveySample = FixtureHelper.loadClassFixtures(SurveyBase[].class);
        SampleSummary sampleSummary = sampleServiceImpl.createSampleSummary(surveySample.get(0));
        sampleSummary.setSampleSummaryPK(SAMPLE_SUMMARY_PK);
        List<SampleUnitBase> sampleUnitBase = FixtureHelper.loadClassFixtures(SampleUnitBase[].class);
        SampleUnit sampleUnit = sampleServiceImpl.createSampleUnit(sampleSummary, sampleUnitBase.get(0));
        assertTrue(sampleUnit.getFormType().equals("112"));
        assertTrue(sampleUnit.getSampleUnitRef().equals("abc"));
        assertTrue(sampleUnit.getSampleUnitType().equals("B"));
        assertTrue(sampleUnit.getSampleSummaryFK() == SAMPLE_SUMMARY_PK);
    }

    @Test
    public void verifySampleUnitsSentToParty() {

    }

    @Test
    public void verifyBadSampleUnitsRejectedByParty() {

    }

    @Test
    public void verifySampleUnitsStateChangedToActive() {

    }

}
