package uk.gov.ons.ctp.response.sample.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import libs.collection.instrument.representation.CollectionInstrumentDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ctp.response.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.client.SurveySvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

@RunWith(MockitoJUnitRunner.class)
public class ValidateSampleSummaryServiceTest {

  @Mock private PartySvcClient partySvcClient;

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Mock private SurveySvcClient surveySvcClient;

  // class under test
  @InjectMocks private ValidateSampleSummaryService validateSampleSummaryService;

  /**
   * Test the happy day path
   *
   * @throws UnknownSampleSummaryException fails the test
   */
  @Test
  public void testValidate() throws UnknownSampleSummaryException {
    String surveyId = UUID.randomUUID().toString();
    UUID sampleSummaryId = UUID.randomUUID();
    String collectionExerciseId = UUID.randomUUID().toString();

    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();
    PartyDTO partyDTO = buildPartyDTO(surveyId);

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

    when(partySvcClient.requestParty(sampleUnitType, sampleUnitRef)).thenReturn(partyDTO);
    when(surveySvcClient.requestClassifierTypeSelectors(surveyId)).thenReturn(classifiers);
    when(surveySvcClient.requestClassifierTypeSelector(surveyId, classifierId))
        .thenReturn(classifierTypeDTO);
    when(collectionInstrumentSvcClient.requestCollectionInstruments(anyString()))
        .thenReturn(collectionInstruments);
    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());
    boolean valid =
        validateSampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
    assertTrue("sample summary should be valid", valid);
    verify(partySvcClient, times(1)).requestParty(sampleUnitType, sampleUnitRef);
  }

  /**
   * Test the validation fails if the party does not exist
   *
   * @throws UnknownSampleSummaryException fails the test
   */
  @Test
  public void testValidateFailsWithUnknownPartyId() throws UnknownSampleSummaryException {
    String surveyId = UUID.randomUUID().toString();
    UUID sampleSummaryId = UUID.randomUUID();
    String collectionExerciseId = UUID.randomUUID().toString();
    String sampleUnitRef = "11111111";
    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setId(sampleSummaryId);
    sampleSummary.setSampleSummaryPK(1);

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);
    sampleUnit.setSampleUnitRef(sampleUnitRef);
    sampleUnit.setSampleUnitType(sampleUnitType);
    List<SampleUnit> samples = new ArrayList<>();
    samples.add(sampleUnit);

    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());
    when(partySvcClient.requestParty(sampleUnitType, sampleUnitRef))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));

    boolean valid =
        validateSampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
    assertFalse("sample summary ", valid);
  }

  /**
   * Test that validation fails with unknown collection instrument
   *
   * @throws UnknownSampleSummaryException fails the test
   */
  @Test
  public void testValidateFailsWithUnknownCollectionInstrument()
      throws UnknownSampleSummaryException {
    String surveyId = UUID.randomUUID().toString();
    UUID sampleSummaryId = UUID.randomUUID();
    String collectionExerciseId = UUID.randomUUID().toString();

    String sampleUnitType = "B";
    UUID sampleUnitId = UUID.randomUUID();
    PartyDTO partyDTO = buildPartyDTO(surveyId);

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

    when(partySvcClient.requestParty(sampleUnitType, sampleUnitRef)).thenReturn(partyDTO);
    when(surveySvcClient.requestClassifierTypeSelectors(surveyId)).thenReturn(classifiers);
    when(surveySvcClient.requestClassifierTypeSelector(surveyId, classifierId))
        .thenReturn(classifierTypeDTO);
    when(collectionInstrumentSvcClient.requestCollectionInstruments(anyString()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not found"));
    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());
    boolean valid =
        validateSampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
    assertFalse("sample summary should not be valid", valid);
    verify(partySvcClient, times(1)).requestParty(sampleUnitType, sampleUnitRef);
  }

  /**
   * Test validation fails if the sample summary is unknown.
   *
   * @throws UnknownSampleSummaryException expected
   */
  @Test(expected = UnknownSampleSummaryException.class)
  public void testValidateFailsWithUnknownSampleSummary() throws UnknownSampleSummaryException {
    String surveyId = UUID.randomUUID().toString();
    UUID sampleSummaryId = UUID.randomUUID();
    String collectionExerciseId = UUID.randomUUID().toString();

    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.ofNullable(null));
    validateSampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
  }

  private PartyDTO buildPartyDTO(String surveyId) {
    PartyDTO partyDTO = new PartyDTO();
    partyDTO.setId(UUID.randomUUID().toString());

    Enrolment enrolment = new Enrolment();
    enrolment.setEnrolmentStatus("ENABLED");
    List<Enrolment> enrolments = new ArrayList<>();
    enrolments.add(enrolment);
    enrolment.setSurveyId(surveyId);

    Association association = new Association();
    association.setEnrolments(enrolments);

    List<Association> associations = new ArrayList<>();
    associations.add(association);
    partyDTO.setAssociations(associations);
    return partyDTO;
  }
}
