package uk.gov.ons.ctp.response.sample.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SampleServiceImplTest {

    @InjectMocks
    private SampleServiceImpl sampleServiceImpl;

    private List<SurveyBase> surveySample;
    private List<SampleUnitBase> sampleUnitList;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        surveySample = FixtureHelper.loadClassFixtures(SurveyBase[].class);
        sampleUnitList = FixtureHelper.loadClassFixtures(SampleUnitBase[].class);

    }

    private static final int SAMPLE_SUMMARY_PK = 1;

    @Test
    public void verifySampleSummaryCreatedCorrectly() throws Exception {
        SampleSummary sampleSummary = sampleServiceImpl.createSampleSummary(surveySample.get(0));
        assertTrue(sampleSummary.getSurveyRef().equals("abc"));
        assertNotNull(sampleSummary.getIngestDateTime());
        assertTrue(sampleSummary.getEffectiveEndDateTime().getTime() == 1583743600000L);
        assertTrue(sampleSummary.getEffectiveStartDateTime().getTime() == 1483743600000L);
        assertTrue(sampleSummary.getState() == SampleSummaryDTO.SampleState.INIT);
    }

}
