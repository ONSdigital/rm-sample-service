package uk.gov.ons.ctp.response.sample.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import libs.collection.instrument.representation.CollectionInstrumentDTO;
import libs.common.state.StateTransitionManager;
import libs.party.representation.Association;
import libs.party.representation.Enrolment;
import libs.party.representation.PartyDTO;
import libs.survey.representation.SurveyClassifierDTO;
import libs.survey.representation.SurveyClassifierTypeDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ctp.response.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.client.SurveySvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryEnrichmentServiceTest {

  @Mock private PartySvcClient partySvcClient;

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent>
      sampleSummaryTransitionManager;

  @Mock
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
      sampleUnitTransitionManager;

  // class under test
  @InjectMocks private SampleSummaryEnrichmentService sampleSummaryEnrichmentService;

  /**
   * Test the happy day path
   *
   * @throws UnknownSampleSummaryException fails the test
   */
  @Test
  public void testEnrich() throws UnknownSampleSummaryException {
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collectionExerciseId = UUID.randomUUID();

    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();
    PartyDTO partyDTO = buildPartyDTO(surveyId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);
    String sampleUnitRef = "11111111";
    sampleUnit.setSampleUnitRef(sampleUnitRef);
    sampleUnit.setSampleUnitType(sampleUnitType);
    sampleUnit.setState(SampleUnitDTO.SampleUnitState.PERSISTED);
    List<SampleUnit> samples = new ArrayList<>();
    samples.add(sampleUnit);

    List<SurveyClassifierDTO> classifiers = new ArrayList<>();
    SurveyClassifierDTO surveyClassifierDTO = new SurveyClassifierDTO();
    surveyClassifierDTO.setName("COLLECTION_INSTRUMENT");
    UUID classifierId = UUID.randomUUID();
    surveyClassifierDTO.setId(classifierId.toString());
    classifiers.add(surveyClassifierDTO);

    SurveyClassifierTypeDTO classifierTypeDTO = new SurveyClassifierTypeDTO();
    classifierTypeDTO.setId(classifierId.toString());
    classifierTypeDTO.setName("COLLECTION_INSTRUMENT");
    List<String> classifierTypes = new ArrayList<>();
    classifierTypes.add("FORM_TYPE");
    classifierTypeDTO.setClassifierTypes(classifierTypes);

    UUID collectionInstrumentId = UUID.randomUUID();
    CollectionInstrumentDTO collectionInstrumentDTO = new CollectionInstrumentDTO();
    collectionInstrumentDTO.setId(collectionInstrumentId);
    List<CollectionInstrumentDTO> collectionInstruments = new ArrayList<>();
    collectionInstruments.add(collectionInstrumentDTO);

    when(partySvcClient.requestParty(sampleUnitRef)).thenReturn(partyDTO);
    when(surveySvcClient.requestClassifierTypeSelectors(surveyId)).thenReturn(classifiers);
    when(surveySvcClient.requestClassifierTypeSelector(surveyId, classifierId))
        .thenReturn(classifierTypeDTO);
    when(collectionInstrumentSvcClient.requestCollectionInstruments(anyString()))
        .thenReturn(collectionInstruments);
    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());
    boolean enriched =
        sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);
    assertTrue("sample summary should be enriched", enriched);
    verify(partySvcClient, times(1)).requestParty(sampleUnitRef);
  }

  /**
   * Test the enrichment fails if the party does not exist
   *
   * @throws Exception any exception fails the test
   */
  @Test
  public void testEnrichFailsWithUnknownPartyId() throws Exception {
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collectionExerciseId = UUID.randomUUID();
    String sampleUnitRef = "11111111";
    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);
    sampleUnit.setSampleUnitRef(sampleUnitRef);
    sampleUnit.setSampleUnitType(sampleUnitType);
    sampleUnit.setState(SampleUnitDTO.SampleUnitState.PERSISTED);
    List<SampleUnit> samples = new ArrayList<>();
    samples.add(sampleUnit);

    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());
    when(partySvcClient.requestParty(sampleUnitRef))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));
    when(sampleSummaryTransitionManager.transition(
            SampleSummaryDTO.SampleState.ACTIVE, SampleSummaryDTO.SampleEvent.FAIL_VALIDATION))
        .thenReturn(SampleSummaryDTO.SampleState.FAILED);
    when(sampleUnitTransitionManager.transition(
            SampleUnitDTO.SampleUnitState.PERSISTED, SampleUnitDTO.SampleUnitEvent.FAIL_VALIDATION))
        .thenReturn(SampleUnitDTO.SampleUnitState.FAILED);

    boolean enriched =
        sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);
    assertFalse("sample summary ", enriched);

    assertEquals(SampleSummaryDTO.SampleState.FAILED, sampleSummary.getState());
    assertEquals(SampleUnitDTO.SampleUnitState.FAILED, sampleUnit.getState());

    verify(sampleSummaryTransitionManager, times(1))
        .transition(
            SampleSummaryDTO.SampleState.ACTIVE, SampleSummaryDTO.SampleEvent.FAIL_VALIDATION);
    verify(sampleUnitTransitionManager, times(1))
        .transition(
            SampleUnitDTO.SampleUnitState.PERSISTED, SampleUnitDTO.SampleUnitEvent.FAIL_VALIDATION);
  }

  /**
   * Test that enrichment fails with unknown collection instrument
   *
   * @throws any exception fails the test
   */
  @Test
  public void testEnrichFailsWithUnknownCollectionInstrument() throws Exception {
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collectionExerciseId = UUID.randomUUID();

    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();
    PartyDTO partyDTO = buildPartyDTO(surveyId);

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);
    String sampleUnitRef = "11111111";
    sampleUnit.setSampleUnitRef(sampleUnitRef);
    sampleUnit.setSampleUnitType(sampleUnitType);
    sampleUnit.setState(SampleUnitDTO.SampleUnitState.PERSISTED);

    List<SampleUnit> samples = new ArrayList<>();
    samples.add(sampleUnit);

    List<SurveyClassifierDTO> classifiers = new ArrayList<>();
    SurveyClassifierDTO surveyClassifierDTO = new SurveyClassifierDTO();
    surveyClassifierDTO.setName("COLLECTION_INSTRUMENT");
    UUID classifierId = UUID.randomUUID();
    surveyClassifierDTO.setId(classifierId.toString());
    classifiers.add(surveyClassifierDTO);

    SurveyClassifierTypeDTO classifierTypeDTO = new SurveyClassifierTypeDTO();
    classifierTypeDTO.setId(classifierId.toString());
    classifierTypeDTO.setName("COLLECTION_INSTRUMENT");
    List<String> classifierTypes = new ArrayList<>();
    classifierTypes.add("FORM_TYPE");
    classifierTypeDTO.setClassifierTypes(classifierTypes);

    when(partySvcClient.requestParty(sampleUnitRef)).thenReturn(partyDTO);
    when(surveySvcClient.requestClassifierTypeSelectors(surveyId)).thenReturn(classifiers);
    when(surveySvcClient.requestClassifierTypeSelector(surveyId, classifierId))
        .thenReturn(classifierTypeDTO);
    when(collectionInstrumentSvcClient.requestCollectionInstruments(anyString()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));
    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());

    when(sampleSummaryTransitionManager.transition(
            SampleSummaryDTO.SampleState.ACTIVE, SampleSummaryDTO.SampleEvent.FAIL_VALIDATION))
        .thenReturn(SampleSummaryDTO.SampleState.FAILED);
    when(sampleUnitTransitionManager.transition(
            SampleUnitDTO.SampleUnitState.PERSISTED, SampleUnitDTO.SampleUnitEvent.FAIL_VALIDATION))
        .thenReturn(SampleUnitDTO.SampleUnitState.FAILED);
    boolean enriched =
        sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);

    assertFalse("sample summary should not be enriched", enriched);
    assertEquals(SampleSummaryDTO.SampleState.FAILED, sampleSummary.getState());
    assertEquals(SampleUnitDTO.SampleUnitState.FAILED, sampleUnit.getState());

    verify(partySvcClient, times(1)).requestParty(sampleUnitRef);
    verify(sampleSummaryTransitionManager, times(1))
        .transition(
            SampleSummaryDTO.SampleState.ACTIVE, SampleSummaryDTO.SampleEvent.FAIL_VALIDATION);
    verify(sampleUnitTransitionManager, times(1))
        .transition(
            SampleUnitDTO.SampleUnitState.PERSISTED, SampleUnitDTO.SampleUnitEvent.FAIL_VALIDATION);
  }

  /**
   * Test enrichedation fails if the sample summary is unknown.
   *
   * @throws UnknownSampleSummaryException expected
   */
  @Test(expected = UnknownSampleSummaryException.class)
  public void testEnrichFailsWithUnknownSampleSummary() throws UnknownSampleSummaryException {
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collectionExerciseId = UUID.randomUUID();

    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.ofNullable(null));
    sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);
  }

  @Test
  public void testEnrichSampleSummary() throws UnknownSampleSummaryException {
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collectionExerciseId = UUID.randomUUID();

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);

    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);

    assertEquals(surveyId, sampleSummary.getSurveyId());
    assertEquals(collectionExerciseId, sampleSummary.getCollectionExerciseId());
  }

  private PartyDTO buildPartyDTO(UUID surveyId) {
    PartyDTO partyDTO = new PartyDTO();
    partyDTO.setId(UUID.randomUUID().toString());

    Enrolment enrolment = new Enrolment();
    enrolment.setEnrolmentStatus("ENABLED");
    List<Enrolment> enrolments = new ArrayList<>();
    enrolments.add(enrolment);
    enrolment.setSurveyId(surveyId.toString());

    Association association = new Association();
    association.setEnrolments(enrolments);

    List<Association> associations = new ArrayList<>();
    associations.add(association);
    partyDTO.setAssociations(associations);
    return partyDTO;
  }
}
