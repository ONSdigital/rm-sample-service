package uk.gov.ons.ctp.response.sample.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import libs.party.representation.PartyDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;

@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryServiceTest {

  @Mock private PartySvcClient partySvcClient;

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  // class under test
  @InjectMocks private SampleSummaryService sampleSummaryService;

  @Test
  public void testValidate() throws UnknownSampleSummaryException {
    String surveyId = UUID.randomUUID().toString();
    UUID sampleSummaryId = UUID.randomUUID();
    String collectionExerciseId = UUID.randomUUID().toString();

    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();
    PartyDTO partyDTO = new PartyDTO();
    partyDTO.setId(UUID.randomUUID().toString());

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);
    String sampleUnitRef = "11111111";
    sampleUnit.setSampleUnitRef(sampleUnitRef);
    sampleUnit.setSampleUnitType(sampleUnitType);
    List<SampleUnit> samples = new ArrayList<>();
    samples.add(sampleUnit);

    when(partySvcClient.requestParty(sampleUnitType, sampleUnitRef)).thenReturn(partyDTO);
    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFK(1)).thenReturn(samples);
    sampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
    verify(partySvcClient, times(1)).requestParty(sampleUnitType, sampleUnitRef);
  }
}
