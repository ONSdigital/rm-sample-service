package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.response.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.client.SurveySvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.validation.CollectionInstrumentClassifierTypes;

/**
 * Performs actions on a sample summary, e.g. validates its complete and updates with any additional
 * data required
 *
 * <p>Note: this is a rework of the Validate sample in the collection exercise and as such some of
 * the code has been copied and adapted to fit the sample service
 */
@Service
public class SampleSummaryEnrichmentService {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryEnrichmentService.class);

  private static final String ENABLED = "ENABLED";

  private static final String CASE_TYPE_SELECTOR = "COLLECTION_INSTRUMENT";

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private PartySvcClient partySvcClient;

  @Autowired private SurveySvcClient surveySvcClient;

  @Autowired private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Transactional(propagation = Propagation.REQUIRED)
  public boolean enrich(UUID surveyId, UUID sampleSummaryId, UUID collectionExerciseId)
      throws UnknownSampleSummaryException {

    // first find the correct sample summary
    SampleSummary sampleSummary =
        sampleSummaryRepository
            .findById(sampleSummaryId)
            .orElseThrow(() -> new UnknownSampleSummaryException());
    LOG.debug("found sample summary", kv("sampleSummaryId", sampleSummaryId));

    /* Link the sample summary and collection exercise.  This is something that needs to be changed down the line as
     * it's odd that the attributes in party have a sampleSummaryId (which is only ever going to be for a specific
     * collection exercise) but not the collectionExerciseId.
     * The collectionExerciseId should be set by the sample-file-uploader as it's talking to party to create the
     * business attributes. */
    partySvcClient.linkSampleSummaryId(sampleSummaryId.toString(), collectionExerciseId);

    enrichSampleSummary(sampleSummary, surveyId, collectionExerciseId);

    // get all the samples
    Stream<SampleUnit> sampleUnits =
        sampleUnitRepository.findBySampleSummaryFKAndState(
            sampleSummary.getSampleSummaryPK(), SampleUnitDTO.SampleUnitState.PERSISTED);

    LOG.debug("found samples for sample summary", kv("sampleSummaryId", sampleSummaryId));
    // create a map to hold form types to collection instrument ids
    Map<String, Optional<UUID>> formTypeMap = new HashMap<>();

    List<UUID> invalidSamples = new ArrayList<>();
    sampleUnits.forEach(
        (sampleUnit) -> {
          UUID sampleUnitId = sampleUnit.getId();
          LOG.debug(
              "processing sample unit",
              kv("sampleUnitId", sampleUnitId),
              kv("sampleSummaryId", sampleSummaryId));
          // for each sample check there is a party id
          LOG.debug(
              "about to find party",
              kv("sampleUnitId", sampleUnitId),
              kv("sampleSummaryId", sampleSummaryId));
          boolean foundParty = findAndUpdateParty(surveyId, sampleUnit, sampleUnitId);
          LOG.debug(
              "party found",
              kv("sampleUnitId", sampleUnitId),
              kv("sampleSummaryId", sampleSummaryId),
              kv("foundParty", foundParty));
          if (foundParty) {
            LOG.debug(
                "about to search for collection instrument id",
                kv("sampleUnitId", sampleUnitId),
                kv("sampleSummaryId", sampleSummaryId));
            boolean foundCI = findAndUpdateCollectionInstrument(surveyId, formTypeMap, sampleUnit);
            LOG.debug(
                "CI found",
                kv("sampleUnitId", sampleUnitId),
                kv("sampleSummaryId", sampleSummaryId),
                kv("foundCI", foundCI));
            if (!foundCI) {
              invalidSamples.add(sampleUnitId);
            }
          } else {
            invalidSamples.add(sampleUnitId);
          }
          sampleUnitRepository.saveAndFlush(sampleUnit);
        });

    // if there are invalid samples then it is not validated
    return invalidSamples.isEmpty();
  }

  private boolean findAndUpdateCollectionInstrument(
      UUID surveyId, Map<String, Optional<UUID>> formTypeMap, SampleUnit sampleUnit) {
    // now find the Collection instrument for this sample
    // and if we haven't seen this form type before add it
    // to a map so we can reuse for the next sample

    Optional<UUID> collectionInstrumentId =
        formTypeMap.computeIfAbsent(
            sampleUnit.getFormType(),
            key -> {
              UUID ciId = null;
              List<String> classifierTypes = requestSurveyClassifiers(surveyId);
              try {
                ciId = requestCollectionInstrumentId(classifierTypes, sampleUnit, surveyId);
              } catch (HttpClientErrorException e) {
                if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                  LOG.error(
                      "Unexpected HTTP response code from collection instrument",
                      kv("sample_unit", sampleUnit),
                      kv("status_code", e.getStatusCode()));
                  throw e;
                } else {
                  LOG.warn(
                      "Unable to find collection instrument id",
                      kv("sample_unit", sampleUnit),
                      kv("status_code", e.getStatusCode()));
                }
              }
              return Optional.ofNullable(ciId);
            });
    // If we could find the CI, then set it on the sample (or it will fail validation)
    if (collectionInstrumentId.isPresent()) {
      sampleUnit.setCollectionInstrumentId(collectionInstrumentId.get());
    } else {
      LOG.warn(
          "invalid sample unable to find collection instrument id for sample ",
          kv("sampleId", sampleUnit.getId()));
    }
    return collectionInstrumentId.isPresent();
  }

  private boolean findAndUpdateParty(UUID surveyId, SampleUnit sampleUnit, UUID sampleUnitId) {
    PartyDTO party = getParty(sampleUnit.getSampleUnitRef());
    boolean foundParty = (party != null && party.getId() != null);
    if (foundParty) {
      // save the party id against the sample
      String partyId = party.getId();
      LOG.debug("found party id", kv("partyId", partyId), kv("sampleUnitId", sampleUnitId));
      sampleUnit.setPartyId(UUID.fromString(partyId));

      // then use that party object to see if there are active enrolments
      boolean activeEnrolment = hasActiveEnrolment(party, surveyId);
      LOG.debug(
          "has active enrolment",
          kv("activeEnrolment", activeEnrolment),
          kv("sampleId", sampleUnitId),
          kv("partyId", partyId));
      sampleUnit.setActiveEnrolment(activeEnrolment);
    } else {
      LOG.warn("invalid sample unable to find party id for sample ", kv("sampleId", sampleUnitId));
    }
    return foundParty;
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
      List<String> classifierTypes, SampleUnit sampleUnit, UUID surveyId) {
    Map<String, String> classifiers = new HashMap<>();
    classifiers.put("SURVEY_ID", surveyId.toString());

    // for all the classifiers returned by the survey service for this survey
    // get the ids from the sample unit
    // this is likely to be form type
    for (String classifier : classifierTypes) {
      try {
        CollectionInstrumentClassifierTypes classifierType =
            CollectionInstrumentClassifierTypes.valueOf(classifier);
        classifiers.put(classifierType.name(), classifierType.apply(sampleUnit));
      } catch (IllegalArgumentException e) {
        LOG.warn("Classifier not supported", kv("classifier", classifier), e);
      }
    }

    // once we know the classifiers, e.g. survey id and form type
    // construct a json search string and send to collection instrument
    String searchString = convertToJSON(classifiers);
    List<CollectionInstrumentDTO> collectionInstruments =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    UUID collectionInstrumentId = null;
    if (!collectionInstruments.isEmpty()) {
      if (collectionInstruments.size() > 1) {
        LOG.warn(
            "Multiple collection instruments found, taking most recent first",
            kv("collectionInstrumentsFound", collectionInstruments.size()),
            kv("searchString", searchString));
      }
      collectionInstrumentId = collectionInstruments.get(0).getId();
    } else {
      LOG.error("No collection instruments found", kv("search_string", searchString));
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

  /**
   * Return the party object for a specific sample
   *
   * @return the party object
   */
  private PartyDTO getParty(String sampleUnitRef) {
    try {
      PartyDTO party = partySvcClient.requestParty(sampleUnitRef);
      return party;
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
        LOG.error(
            "Unexpected HTTP response code from party service",
            kv("sampleUnit", sampleUnitRef),
            kv("status_code", e.getStatusCode()));
        throw e;
      } else {
        LOG.error("party does not exist for sample", kv("sampleUnit", sampleUnitRef));
        return null;
      }
    }
  }

  private boolean hasActiveEnrolment(PartyDTO party, UUID surveyId) {
    List<Enrolment> enrolments =
        party.getAssociations().stream()
            .map(Association::getEnrolments)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    return enrolments.stream()
        .anyMatch(enrolment -> enrolmentIsEnabledForSurvey(enrolment, surveyId));
  }

  private boolean enrolmentIsEnabledForSurvey(final Enrolment enrolment, UUID surveyId) {
    return enrolment.getSurveyId().equals(surveyId.toString())
        && enrolment.getEnrolmentStatus().equalsIgnoreCase(ENABLED);
  }

  /**
   * Request the classifier type selectors from the Survey service.
   *
   * @param surveyId for which to get collection instrument classifier selectors.
   * @return List<String> Survey classifier type selectors for exercise
   */
  private List<String> requestSurveyClassifiers(UUID surveyId) {

    SurveyClassifierTypeDTO surveyClassifierType;

    // Call Survey Service and get classifier types
    List<SurveyClassifierDTO> surveyClassifiers =
        surveySvcClient.requestClassifierTypeSelectors(surveyId);

    // select the one that matches COLLECTION_INSTRUMENT
    SurveyClassifierDTO chosenClassifier =
        surveyClassifiers.stream()
            .filter(surveyClassifier -> CASE_TYPE_SELECTOR.equals(surveyClassifier.getName()))
            .findAny()
            .orElse(null);

    // re call the survey service with the chosen classifier i.e. collection instrument
    // in order to get the classifier types e.g. FORM_TYPE
    if (chosenClassifier != null) {
      surveyClassifierType =
          surveySvcClient.requestClassifierTypeSelector(
              surveyId, UUID.fromString(chosenClassifier.getId()));
      if (surveyClassifierType != null) {
        return surveyClassifierType.getClassifierTypes();
      } else {
        LOG.error(
            "Error requesting Survey Classifier Types",
            kv("surveyId", surveyId),
            kv("classifierId", chosenClassifier.getId()));
        throw new IllegalStateException("Error requesting Survey Classifier Types");
      }
    } else {
      LOG.error("Error requesting Survey Classifier Types", kv("surveyId", surveyId));
      throw new IllegalStateException("Error requesting Survey Classifier Types");
    }
  }

  private void enrichSampleSummary(
      SampleSummary sampleSummary, UUID surveyId, UUID collectionExerciseId) {
    sampleSummary.setCollectionExerciseId(collectionExerciseId);
    sampleSummary.setSurveyId(surveyId);
    sampleSummaryRepository.saveAndFlush(sampleSummary);
  }
}
