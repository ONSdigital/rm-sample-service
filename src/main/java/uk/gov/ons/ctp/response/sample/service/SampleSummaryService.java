package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import libs.party.representation.Association;
import libs.party.representation.Enrolment;
import libs.party.representation.PartyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ctp.response.client.PartySvcClient;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;

/**
 * Performs actions on a sample summary, e.g. validates its complete and updates with any additional
 * data required
 */
@Service
public class SampleSummaryService {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryService.class);

  private static final String ENABLED = "ENABLED";

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired private PartySvcClient partySvcClient;

  public boolean validate(String surveyId, UUID sampleSummaryId, String collectionExerciseId)
      throws UnknownSampleSummaryException {

    SampleSummary sampleSummary =
        sampleSummaryRepository
            .findById(sampleSummaryId)
            .orElseThrow(() -> new UnknownSampleSummaryException());

    List<SampleUnit> samples =
        sampleUnitRepository.findBySampleSummaryFK(sampleSummary.getSampleSummaryPK());
    // TODO implement a stream here

    for (SampleUnit sampleUnit : samples) {
      PartyDTO party = getParty(sampleUnit);
      String partyId = party.getId();
      if (partyId != null) {
        sampleUnit.setPartyId(UUID.fromString(partyId));
      } else {
        // sample not valid
      }
      if (hasActiveEnrolment(party, surveyId)) {
        sampleUnit.setActivEnrolment(true);
      }

      sampleUnitRepository.saveAndFlush(sampleUnit);
    }
    return false;
  }

  private String getCollectionInstrument() {
    return null;
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

  private String getClassifiers() {
    return null;
  }
}
