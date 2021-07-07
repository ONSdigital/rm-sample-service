package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.*;
import java.util.stream.Collectors;
import libs.collection.instrument.representation.CollectionInstrumentDTO;
import libs.party.representation.Association;
import libs.party.representation.Enrolment;
import libs.party.representation.PartyDTO;
import libs.survey.representation.SurveyClassifierDTO;
import libs.survey.representation.SurveyClassifierTypeDTO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.client.SurveySvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.validation.CollectionInstrumentClassifierTypes;

/**
 * Performs actions on a sample summary, e.g. validates its complete and updates with any additional
 * data required
 */
@Service
public class SampleSummaryService {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryService.class);

  private static final String ENABLED = "ENABLED";

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private PartySvcClient partySvcClient;

  @Autowired private SurveySvcClient surveySvcClient;

  @Autowired private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  public boolean validate(String surveyId, UUID sampleSummaryId, String collectionExerciseId)
      throws UnknownSampleSummaryException {

    SampleSummary sampleSummary =
        sampleSummaryRepository
            .findById(sampleSummaryId)
            .orElseThrow(() -> new UnknownSampleSummaryException());

    List<SampleUnit> samples =
        sampleUnitRepository.findBySampleSummaryFK(sampleSummary.getSampleSummaryPK());
    // TODO implement a stream here

    Map<String, Optional<UUID>> formTypeMap = new HashMap<>();

    for (SampleUnit sampleUnit : samples) {
      PartyDTO party = getParty(sampleUnit);
      String partyId = party.getId();
      if (partyId != null) {
        sampleUnit.setPartyId(UUID.fromString(partyId));
      } else {
        // TODO sample not valid
      }
      if (hasActiveEnrolment(party, surveyId)) {
        sampleUnit.setActiveEnrolment(true);
      }

      // now do the CI
      // If we haven't seen this form type before, add the CI to the cache if we can find it
      Optional<UUID> collectionInstrumentId =
          formTypeMap.computeIfAbsent(
              sampleUnit.getFormType(),
              key -> {
                UUID returnValue = null;
                List<String> classifierTypes = requestSurveyClassifiers(surveyId);
                try {
                  returnValue =
                      requestCollectionInstrumentId(classifierTypes, sampleUnit, surveyId);
                } catch (HttpClientErrorException e) {
                  if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                    LOG.error(
                        "Unexpected HTTP response code from collection instrument",
                        kv("sample_unit", sampleUnit),
                        kv("status_code", e.getStatusCode()));
                    throw e; // Re-throw anything that's not a 404 so that we retry
                  }
                }

                return Optional.ofNullable(returnValue);
              });

      // If we could find the CI, then set it on the sample (or it will fail validation)
      if (collectionInstrumentId.isPresent()) {
        sampleUnit.setCollectionInstrumentId(collectionInstrumentId.get());
      }

      sampleUnitRepository.saveAndFlush(sampleUnit);
    }
    return false;
  }

  /**
   * Request the Collection Instrument details from the Collection Instrument Service using the
   * given classifiers and return the instrument Id.
   *
   * @param classifierTypes used in search by Collection Instrument service to return instrument
   *     details matching classifiers.
   * @param sampleUnit to which the collection instrument relates.
   * @return UUID of collection instrument or null if not found.
   * @throws RestClientException something went wrong making http call
   */
  private UUID requestCollectionInstrumentId(
      List<String> classifierTypes, SampleUnit sampleUnit, String surveyId) {
    Map<String, String> classifiers = new HashMap<>();
    classifiers.put("SURVEY_ID", surveyId);
    for (String classifier : classifierTypes) {
      try {
        CollectionInstrumentClassifierTypes classifierType =
            CollectionInstrumentClassifierTypes.valueOf(classifier);
        classifiers.put(classifierType.name(), classifierType.apply(sampleUnit));
      } catch (IllegalArgumentException e) {
        LOG.warn("Classifier not supported", kv("classifier", classifier), e);
      }
    }
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> collectionInstruments =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    UUID collectionInstrumentId;
    if (collectionInstruments.isEmpty()) {
      LOG.error("No collection instruments found", kv("search_string", searchString));
      collectionInstrumentId = null;
    } else if (collectionInstruments.size() > 1) {
      LOG.warn(
          "Multiple collection instruments found, taking most recent first",
          kv("collection_instruments_found", collectionInstruments.size()),
          kv("search_string", searchString));
      collectionInstrumentId = collectionInstruments.get(0).getId();
    } else {
      collectionInstrumentId = collectionInstruments.get(0).getId();
    }

    return collectionInstrumentId;
  }

  /**
   * Convert map of classifier types and values to JSON search string.
   *
   * @param classifiers classifier types and values from which to construct search String.
   * @return JSON string used in search.
   */
  private String convertToJSON(Map<String, String> classifiers) {
    JSONObject searchString = new JSONObject(classifiers);
    return searchString.toString();
  }

  private PartyDTO getParty(SampleUnit sampleUnit) {
    try {
      PartyDTO party =
          partySvcClient.requestParty(
              sampleUnit.getSampleUnitType(), sampleUnit.getSampleUnitRef());
      return party;
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
        LOG.error(
            "Unexpected HTTP response code from party service",
            kv("sampleUnit", sampleUnit),
            kv("status_code", e.getStatusCode()));
        throw e;
      } else {
        LOG.error("party does not exist for sample", kv("sampleUnit", sampleUnit));
        return null;
      }
    }
  }

  private boolean hasActiveEnrolment(PartyDTO party, String surveyId) {
    List<Enrolment> enrolments =
        party.getAssociations().stream()
            .map(Association::getEnrolments)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    return enrolments.stream()
        .anyMatch(enrolment -> enrolmentIsEnabledForSurvey(enrolment, surveyId));
  }

  private boolean enrolmentIsEnabledForSurvey(final Enrolment enrolment, String surveyId) {
    return enrolment.getSurveyId().equals(surveyId)
        && enrolment.getEnrolmentStatus().equalsIgnoreCase(ENABLED);
  }

  /**
   * Request the classifier type selectors from the Survey service.
   *
   * @param surveyId for which to get collection instrument classifier selectors.
   * @return List<String> Survey classifier type selectors for exercise
   */
  private List<String> requestSurveyClassifiers(String surveyId) {

    SurveyClassifierTypeDTO classifierTypeSelector;

    // Call Survey Service
    // Get Classifier types for Collection Instruments
    List<SurveyClassifierDTO> classifierTypeSelectors =
        surveySvcClient.requestClassifierTypeSelectors(surveyId);
    SurveyClassifierDTO chosenSelector =
        classifierTypeSelectors.stream()
            .filter(classifierType -> CASE_TYPE_SELECTOR.equals(classifierType.getName()))
            .findAny()
            .orElse(null);
    if (chosenSelector != null) {
      classifierTypeSelector =
          surveySvcClient.requestClassifierTypeSelector(
              surveyId, UUID.fromString(chosenSelector.getId()));
      if (classifierTypeSelector != null) {
        return classifierTypeSelector.getClassifierTypes();
      } else {
        LOG.error(
            "Error requesting Survey Classifier Types",
            kv("survey_id", surveyId),
            kv("case_type_selector_id", chosenSelector.getId()));
        throw new IllegalStateException("Error requesting Survey Classifier Types");
      }
    } else {
      LOG.error("Error requesting Survey Classifier Types", kv("survey_id", surveyId));
      throw new IllegalStateException("Error requesting Survey Classifier Types");
    }
  }
}
