package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
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
    List<String> classiferTypes = new ArrayList<>();
    classiferTypes.add("FORM_TYPE");
    classifierTypeDTO.setClassifierTypes(classiferTypes);

    when(partySvcClient.requestParty(sampleUnitType, sampleUnitRef)).thenReturn(partyDTO);
    when(surveySvcClient.requestClassifierTypeSelectors(surveyId)).thenReturn(classifiers);
    when(surveySvcClient.requestClassifierTypeSelector(surveyId, classifierId))
        .thenReturn(classifierTypeDTO);
    when(sampleSummaryRepository.findById(sampleSummaryId)).thenReturn(Optional.of(sampleSummary));
    when(sampleUnitRepository.findBySampleSummaryFKAndState(
            1, SampleUnitDTO.SampleUnitState.PERSISTED))
        .thenReturn(samples.stream());
    validateSampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
    verify(partySvcClient, times(1)).requestParty(sampleUnitType, sampleUnitRef);
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
