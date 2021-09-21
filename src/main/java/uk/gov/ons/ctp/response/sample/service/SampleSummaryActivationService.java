package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.message.SampleSummaryActivationStatusPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryActivationDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryStatusDTO;

@Service
public class SampleSummaryActivationService {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryEnrichmentService.class);

  @Autowired private SampleSummaryDistributionService sampleSummaryDistributionService;

  @Autowired private SampleSummaryEnrichmentService sampleSummaryEnrichmentService;

  @Autowired private SampleSummaryActivationStatusPublisher sampleSummaryActivationStatusPublisher;

  /**
   * Sorts out all the sample units for a given sample summary. It does this by first validating and
   * enriching the data, followed by distributing all the sample units to case.
   *
   * <p>During each successful step of the process, a message will be sent to collection exercise so
   * it can know how the sample summary activation is going.
   *
   * @param sampleSummaryActivation an object containing all the details required for activation
   */
  @Async
  public void activateSampleSummaryFromPubsub(SampleSummaryActivationDTO sampleSummaryActivation)
      throws SampleSummaryActivationException {
    LOG.info(
        "Beginning sample summary activation",
        kv("sampleSummaryActivation", sampleSummaryActivation));
    validateAndEnrich(sampleSummaryActivation);

    distribute(sampleSummaryActivation);
    LOG.info(
        "Completed sample summary activation",
        kv("sampleSummaryId", sampleSummaryActivation.getSampleSummaryId()),
        kv("surveyId", sampleSummaryActivation.getSurveyId()),
        kv("collectionExerciseId", sampleSummaryActivation.getCollectionExerciseId()));
  }

  public void sendEnrichStatusToCollectionExercise(UUID collectionExerciseId, boolean successful) {
    SampleSummaryStatusDTO collectionExerciseStatus = new SampleSummaryStatusDTO();
    collectionExerciseStatus.setCollectionExerciseId(collectionExerciseId);
    collectionExerciseStatus.setSuccessful(successful);
    collectionExerciseStatus.setEvent(SampleSummaryStatusDTO.Event.ENRICHED);
    sampleSummaryActivationStatusPublisher.sendSampleSummaryActivation(collectionExerciseStatus);
  }

  public void sendDistributeStatusToCollectionExercise(
      UUID collectionExerciseId, boolean successful) {
    SampleSummaryStatusDTO collectionExerciseStatus = new SampleSummaryStatusDTO();
    collectionExerciseStatus.setCollectionExerciseId(collectionExerciseId);
    collectionExerciseStatus.setSuccessful(successful);
    collectionExerciseStatus.setEvent(SampleSummaryStatusDTO.Event.DISTRIBUTED);
    sampleSummaryActivationStatusPublisher.sendSampleSummaryActivation(collectionExerciseStatus);
  }

  /**
   * Executes the first step of the activation process. This will validate all the sample unit data
   * and enrich the sample unit by gathering any extra data required for the second step of the
   * activation process.
   *
   * @param sampleSummaryActivation an object containing all the details required for activation
   */
  private void validateAndEnrich(SampleSummaryActivationDTO sampleSummaryActivation)
      throws SampleSummaryActivationException {
    UUID sampleSummaryId = sampleSummaryActivation.getSampleSummaryId();
    UUID surveyId = sampleSummaryActivation.getSurveyId();
    UUID collectionExerciseId = sampleSummaryActivation.getCollectionExerciseId();

    LOG.debug(
        "about to enrich sample summary",
        kv("sampleSummaryId", sampleSummaryId),
        kv("surveyId", surveyId),
        kv("collectionExerciseId", collectionExerciseId));

    try {
      boolean validated =
          sampleSummaryEnrichmentService.enrich(surveyId, sampleSummaryId, collectionExerciseId);
      LOG.debug(
          "Enriched sample summary",
          kv("sampleSummaryId", sampleSummaryId),
          kv("surveyId", surveyId),
          kv("collectionExerciseId", collectionExerciseId),
          kv("validated", validated));
      if (validated) {
        LOG.info("Sample summary successfully enriched", kv("sampleSummaryId", sampleSummaryId));
        sendEnrichStatusToCollectionExercise(
            sampleSummaryActivation.getCollectionExerciseId(), true);
      } else {
        LOG.error("Validation and enrichment failed", kv("sampleSummaryId", sampleSummaryId));
        sendEnrichStatusToCollectionExercise(
            sampleSummaryActivation.getCollectionExerciseId(), false);
        throw new SampleSummaryActivationException();
      }

    } catch (UnknownSampleSummaryException e) {
      LOG.error("unknown sample summary id", kv("sampleSummaryId", sampleSummaryId), e);
      sendEnrichStatusToCollectionExercise(collectionExerciseId, false);
      throw new SampleSummaryActivationException(e);
    } catch (SampleSummaryActivationException | RuntimeException e) {
      LOG.error(
          "Something went wrong activating sample summary",
          kv("sampleSummaryId", sampleSummaryId),
          e);
      sendEnrichStatusToCollectionExercise(collectionExerciseId, false);
      throw e;
    }
  }

  /**
   * Executes the second step of the activation process. This will send each sample unit in a sample
   * summary to the case service to create cases for each one (which are used to track the progress
   * of the survey submission).
   *
   * @param sampleSummaryActivation an object containing all the details required for activation
   */
  private void distribute(SampleSummaryActivationDTO sampleSummaryActivation)
      throws SampleSummaryActivationException {
    try {
      sampleSummaryDistributionService.distribute(sampleSummaryActivation.getSampleSummaryId());
      sendDistributeStatusToCollectionExercise(
          sampleSummaryActivation.getCollectionExerciseId(), true);

    } catch (NoSampleUnitsInSampleSummaryException | UnknownSampleSummaryException e) {
      LOG.error(
          "something went wrong during distribution sample units",
          kv("sampleSummaryId", sampleSummaryActivation.getSampleSummaryId()),
          kv("surveyId", sampleSummaryActivation.getSurveyId()),
          kv("collectionExerciseId", sampleSummaryActivation.getCollectionExerciseId()));
      sendDistributeStatusToCollectionExercise(
          sampleSummaryActivation.getCollectionExerciseId(), false);
      throw new SampleSummaryActivationException();
    }
  }
}
