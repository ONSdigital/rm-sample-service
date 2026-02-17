package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;

@Service
public class SampleSummaryDistributionService {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryEnrichmentService.class);

  @Autowired private SampleService sampleService;
  @Autowired private SampleUnitPublisher sampleUnitPublisher;
  @Autowired private SampleSummaryRepository sampleSummaryRepository;
  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
      sampleUnitTransitionManager;

  @PersistenceContext private EntityManager entityManager;

  MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

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
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void distribute(UUID sampleSummaryId)
      throws NoSampleUnitsInSampleSummaryException, UnknownSampleSummaryException {
    LOG.info("about to distribute sample summary", kv("sampleSummaryId", sampleSummaryId));
    logMemoryUsage("about to distribute sample summary", memoryBean);
    // first find the correct sample summary
    SampleSummary sampleSummary =
        sampleSummaryRepository
            .findById(sampleSummaryId)
            .orElseThrow(UnknownSampleSummaryException::new);

    LOG.info("found sample summary", kv("sampleSummary", sampleSummary.getId()));
    logMemoryUsage("found sample summary", memoryBean);
    Stream<SampleUnit> sampleUnits = sampleService.findSampleUnitsBySampleSummary(sampleSummaryId);

    LOG.info("found sample units for summary", kv("sampleSummaryId", sampleSummaryId));
    logMemoryUsage("found sample units for summary", memoryBean);

    int batchSize = 1000;
    List<SampleUnit> batch = new ArrayList<>(batchSize);
    AtomicInteger i = new AtomicInteger(0);

    sampleUnits.forEach(
        sampleUnit -> {
          try {
            distributeSampleUnit(sampleSummary.getCollectionExerciseId(), sampleUnit);
            batch.add(sampleUnit);
            if (batch.size() == batchSize) {
              sampleUnitRepository.saveAll(batch);
              entityManager.flush();
              entityManager.clear();
              batch.clear();
            }
            i.getAndIncrement();
          } catch (RuntimeException ex) {
            LOG.error(
                "Failed to distribute sample unit",
                kv("sampleSummaryId", sampleSummaryId),
                kv("sampleUnitId", sampleUnit.getId()),
                ex);
            throw ex;
          }
        });
    // To save the final batch
    try {
      if (!batch.isEmpty()) {
        sampleUnitRepository.saveAll(batch);
        entityManager.flush();
        entityManager.clear();
      }
    } catch (RuntimeException ex) {
      LOG.error(
          "Failed to save final batch of sample units following distribution",
          kv("sampleSummaryId", sampleSummaryId),
          kv("batchSize", batch.size()),
          ex);
      throw ex;
    }

    if (i.get() == 0) {
      LOG.info(
          "No sample unit groups to distribute for summary",
          kv("sampleSummaryId", sampleSummaryId));
      throw new NoSampleUnitsInSampleSummaryException();
    }
    sampleUnitRepository.flush();
    LOG.info(
        "Distribution was successful.  Marking sample summary for deletion",
        kv("sampleSummaryId", sampleSummaryId));
    sampleSummary.setMarkForDeletion(true);
    logMemoryUsage("sampleSummaryRepository.saveAndFlush (before)", memoryBean);
    sampleSummaryRepository.saveAndFlush(sampleSummary);
    logMemoryUsage("sampleSummaryRepository.saveAndFlush (after)", memoryBean);
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
    try {
      LOG.info(
          "Transitioning state of sampleUnit",
          kv("id", sampleUnit.getId()),
          kv("from", sampleUnit.getState()),
          kv("to", SampleUnitDTO.SampleUnitEvent.DELIVERING));
      SampleUnitDTO.SampleUnitState newState =
          sampleUnitTransitionManager.transition(
              sampleUnit.getState(), SampleUnitDTO.SampleUnitEvent.DELIVERING);
      sampleUnit.setState(newState);
    } catch (CTPException e) {
      LOG.error("Error occurred whilst transitioning state", e);
    }
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

  /**
   * A temporary method to log memory usage at various points in the distribution process to try and
   * identify memory leaks.
   *
   * @param location The location in the code where the memory usage is being logged
   * @param memoryBean Singleton MemoryMXBean to get memory usage information from
   */
  private static void logMemoryUsage(String location, MemoryMXBean memoryBean) {
    MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
    LOG.info(
        "SampleSummaryDistributionService Memory Usage",
        kv("location", location),
        kv("heapInit", heapMemory.getInit() / (1024 * 1024) + " MB"),
        kv("heapUsed", heapMemory.getUsed() / (1024 * 1024) + " MB"),
        kv("heapCommitted", heapMemory.getCommitted() / (1024 * 1024) + " MB"),
        kv("heapMax", heapMemory.getMax() / (1024 * 1024) + " MB"),
        kv("stackInit", nonHeapMemory.getInit() / (1024 * 1024) + " MB"),
        kv("stackUsed", nonHeapMemory.getUsed() / (1024 * 1024) + " MB"),
        kv("stackCommitted", nonHeapMemory.getCommitted() / (1024 * 1024) + " MB"),
        kv("stackMax", nonHeapMemory.getMax() / (1024 * 1024) + " MB"));
  }
}
