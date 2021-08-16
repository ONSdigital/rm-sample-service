package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;

@Service
public class SampleSummaryDistributionService {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryEnrichmentService.class);

  @Autowired private SampleService sampleService;
  @Autowired private SampleUnitPublisher sampleUnitPublisher;
  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  /**
   * Distributes the sample units to the case service to create cases against each sample unit. This
   * is done over pubsub.
   *
   * @param sampleSummaryId The sampleSummary ID
   * @throws NoSampleUnitsInSampleSummaryException Thrown when the sampleSummary has no sample units
   *     in it
   * @throws UnknownSampleSummaryException Thrown when the sampleSummaryId doesn't match any in the
   *     database
   */
  public void distribute(UUID sampleSummaryId)
      throws NoSampleUnitsInSampleSummaryException, UnknownSampleSummaryException {
    // first find the correct sample summary
    SampleSummary sampleSummary =
        sampleSummaryRepository
            .findById(sampleSummaryId)
            .orElseThrow(UnknownSampleSummaryException::new);

    List<SampleUnit> sampleUnits = sampleService.findSampleUnitsBySampleSummary(sampleSummaryId);

    if (sampleUnits.isEmpty()) {
      LOG.info(
          "No sample unit groups to distribute for summary",
          kv("sampleSummaryId", sampleSummaryId));
      throw new NoSampleUnitsInSampleSummaryException();
    }

    // Catch errors distributing sample units so that only failing units are stopped
    sampleUnits.forEach(
        sampleUnit -> {
          try {
            distributeSampleUnit(sampleSummary.getCollectionExerciseId(), sampleUnit);
          } catch (RuntimeException ex) {
            LOG.error(
                "Failed to distribute sample unit", kv("SampleSummaryId", sampleSummaryId), ex);
          }
        });

    // Currently nothing uses this flag, but in the future we'll clean up old samples once they're
    // no longer needed
    LOG.info(
        "Distribution was successful.  Marking sample summary for deletion",
        kv("sampleSummaryId", sampleSummaryId));
    sampleSummary.setMarkForDeletion(true);
    sampleSummaryRepository.saveAndFlush(sampleSummary);
  }

  /**
   * Distribute a SampleUnit to get the case created in the case service. It's serialised using
   * SampleUnitParentDTO as the SampleUnit doesn't have the collection exercise information with it.
   *
   * @param collectionExerciseId Collection exercise id for the sample unit
   * @param sampleUnit for which to distribute sample units
   */
  public void distributeSampleUnit(UUID collectionExerciseId, SampleUnit sampleUnit) {
    SampleUnitParentDTO parent = createSampleUnitParentDTOObject(collectionExerciseId, sampleUnit);
    sampleUnitPublisher.sendSampleUnitToCase(parent);
  }

  /**
   * Takes a populated sample unit and collection exercise ID and returns a populated
   * sampleUnitParentDTO object
   *
   * @param collectionExerciseId A collection exercise ID
   * @param sampleUnit A populated sample unit
   * @return A populated SampleUnitParent Object
   */
  public SampleUnitParentDTO createSampleUnitParentDTOObject(
      UUID collectionExerciseId, SampleUnit sampleUnit) {
    SampleUnitParentDTO parent = new SampleUnitParentDTO();
    parent.setActiveEnrolment(sampleUnit.isActiveEnrolment());
    parent.setId(sampleUnit.getId().toString());
    parent.setSampleUnitRef(sampleUnit.getSampleUnitRef());
    parent.setSampleUnitType(sampleUnit.getSampleUnitType());
    parent.setPartyId(sampleUnit.getPartyId());
    parent.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId());
    parent.setCollectionExerciseId(collectionExerciseId.toString());
    return parent;
  }
}
