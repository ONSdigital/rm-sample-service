package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import libs.common.time.DateTimeUtil;
import libs.party.representation.PartyDTO;
import libs.sample.validation.SampleUnitBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterBusiness;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterCensus;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterSocial;
import uk.gov.ons.ctp.response.sample.message.EventPublisher;
import uk.gov.ons.ctp.response.sample.message.SampleOutboundPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

@Service
@Configuration
public class SampleService {
  private static final Logger log = LoggerFactory.getLogger(SampleService.class);

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleState, SampleEvent> sampleSvcStateTransitionManager;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitState, SampleUnitEvent>
      sampleSvcUnitStateTransitionManager;

  @Autowired private PartySvcClientService partySvcClient;

  @Autowired private SampleOutboundPublisher sampleOutboundPublisher;

  @Autowired private CollectionExerciseJobService collectionExerciseJobService;

  @Autowired private CsvIngesterBusiness csvIngesterBusiness;

  @Autowired private CsvIngesterCensus csvIngesterCensus;

  @Autowired private CsvIngesterSocial csvIngesterSocial;
  @Autowired private EventPublisher eventPublisher;
  @Autowired private SampleAttributesRepository sampleAttributesRepository;

  public List<SampleSummary> findAllSampleSummaries() {
    return sampleSummaryRepository.findAll();
  }

  public SampleSummary findSampleSummary(UUID id) {
    return sampleSummaryRepository.findById(id);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public SampleSummary saveSample(
      SampleSummary sampleSummary,
      List<? extends SampleUnitBase> samplingUnitList,
      SampleUnitState sampleUnitState) {
    int expectedCI = calculateExpectedCollectionInstruments(samplingUnitList);

    sampleSummary.setTotalSampleUnits(samplingUnitList.size());
    sampleSummary.setExpectedCollectionInstruments(expectedCI);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    saveSampleUnits(samplingUnitList, savedSampleSummary, sampleUnitState);

    return savedSampleSummary;
  }

  private Integer calculateExpectedCollectionInstruments(
      List<? extends SampleUnitBase> samplingUnitList) {
    // TODO: get survey classifiers from survey service, currently using formtype for all business
    // surveys
    Set<String> formTypes = new HashSet<>();
    for (SampleUnitBase businessSampleUnit : samplingUnitList) {
      formTypes.add(businessSampleUnit.getFormType());
    }
    return formTypes.size();
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public SampleSummary createAndSaveSampleSummary() {
    SampleSummary sampleSummary = new SampleSummary();

    sampleSummary.setState(SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());

    return sampleSummaryRepository.save(sampleSummary);
  }

  private void saveSampleUnits(
      List<? extends SampleUnitBase> samplingUnitList,
      SampleSummary sampleSummary,
      SampleUnitState sampleUnitState) {
    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleSummaryFK(sampleSummary.getSampleSummaryPK());
      sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
      sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());
      sampleUnit.setFormType(sampleUnitBase.getFormType());
      sampleUnit.setState(sampleUnitState);
      sampleUnit.setId(sampleUnitBase.getSampleUnitId());
      eventPublisher.publishEvent("Sample Init");
      sampleUnitRepository.save(sampleUnit);
    }
  }

  /**
   * Effect a state transition for the target SampleSummary if one is required
   *
   * @param sampleSummaryPK the sampleSummaryPK to be updated
   * @return SampleSummary the updated SampleSummary
   * @throws CTPException if transition errors
   */
  public SampleSummary activateSampleSummaryState(Integer sampleSummaryPK) throws CTPException {
    SampleSummary targetSampleSummary = sampleSummaryRepository.findOne(sampleSummaryPK);
    SampleState newState =
        sampleSvcStateTransitionManager.transition(
            targetSampleSummary.getState(), SampleEvent.ACTIVATED);
    targetSampleSummary.setState(newState);
    targetSampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummaryRepository.saveAndFlush(targetSampleSummary);
    // Notify the outside world the sample upload has finished
    this.sampleOutboundPublisher.sampleUploadFinished(targetSampleSummary);

    return targetSampleSummary;
  }

  public PartyDTO sendToPartyService(PartyCreationRequestDTO partyCreationRequest)
      throws Exception {
    PartyDTO returnedParty = partySvcClient.postParty(partyCreationRequest);
    SampleUnit sampleUnit =
        sampleUnitRepository.findById(
            UUID.fromString(partyCreationRequest.getAttributes().getSampleUnitId()));
    changeSampleUnitState(sampleUnit);
    sampleSummaryStateCheck(sampleUnit);
    return returnedParty;
  }

  private void changeSampleUnitState(SampleUnit sampleUnit) throws CTPException {
    SampleUnitState newState =
        sampleSvcUnitStateTransitionManager.transition(
            sampleUnit.getState(), SampleUnitEvent.PERSISTING);
    sampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(sampleUnit);
  }

  private void sampleSummaryStateCheck(SampleUnit sampleUnit) throws CTPException {
    int partied =
        sampleUnitRepository.countBySampleSummaryFKAndState(
            sampleUnit.getSampleSummaryFK(), SampleUnitState.PERSISTED);
    int total = sampleUnitRepository.countBySampleSummaryFK(sampleUnit.getSampleSummaryFK());
    if (total == partied) {
      activateSampleSummaryState(sampleUnit.getSampleSummaryFK());
    }
  }

  /**
   * Save CollectionExerciseJob to collectionExerciseJob table
   *
   * @param job CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Integer Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  public Integer initialiseCollectionExerciseJob(CollectionExerciseJob job) throws CTPException {
    // Integer sampleUnitsTotal =
    // initialiseSampleUnitsForCollectionExcerciseCollection(job.getSampleSummaryId());
    Integer sampleUnitsTotal = 0;
    SampleSummary sampleSummary = sampleSummaryRepository.findById(job.getSampleSummaryId());
    if (sampleSummary != null && sampleSummary.getTotalSampleUnits() != 0) {
      sampleUnitsTotal = sampleSummary.getTotalSampleUnits();
      collectionExerciseJobService.storeCollectionExerciseJob(job);
    }
    return sampleUnitsTotal;
  }

  public int getSampleSummaryUnitCount(UUID sampleSummaryId) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId);

    if (sampleSummary == null) {
      throw new IllegalArgumentException(
          String.format("Sample summary %s cannot be found", sampleSummaryId));
    } else if (sampleSummary.getTotalSampleUnits() == null) {
      throw new IllegalStateException(
          String.format("Sample summary %s has no total sample units set", sampleSummaryId));
    }

    int sampleUnitsTotal = sampleSummary.getTotalSampleUnits().intValue();

    return sampleUnitsTotal;
  }

  public SampleSummary ingest(
      final SampleSummary sampleSummary, final MultipartFile file, final String type)
      throws Exception {
    this.sampleOutboundPublisher.sampleUploadStarted(sampleSummary);

    SampleSummary result;
    switch (type.toUpperCase()) {
      case "B":
        result = csvIngesterBusiness.ingest(sampleSummary, file);
        break;
      case "CENSUS":
        result = csvIngesterCensus.ingest(sampleSummary, file);
        break;
      case "SOCIAL":
        result = csvIngesterSocial.ingest(sampleSummary, file);
        break;
      default:
        throw new UnsupportedOperationException(String.format("Type %s not implemented", type));
    }

    return result;
  }

  public Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, String message) {
    try {
      SampleState newState =
          sampleSvcStateTransitionManager.transition(
              sampleSummary.getState(), SampleEvent.FAIL_VALIDATION);
      sampleSummary.setState(newState);
      sampleSummary.setNotes(message);
      SampleSummary persisted = this.sampleSummaryRepository.save(sampleSummary);

      return Optional.of(persisted);
    } catch (CTPException e) {
      log.error(
          "Failed to put sample summary into FAILED state",
          kv("sample_summary", sampleSummary.getId()),
          kv("exception", e));

      return Optional.empty();
    } catch (RuntimeException e) {
      // Hibernate throws RuntimeException if any issue persisting the SampleSummary. This is to
      // ensure it is logged
      // (otherwise they just disappear).
      log.error("Failed to persist sample summary - {}", e);

      throw e;
    }
  }

  public Optional<SampleSummary> failSampleSummary(
      SampleSummary sampleSummary, Exception exception) {
    return failSampleSummary(sampleSummary, exception.getMessage());
  }

  // TODO get this to get the attributes in a separate call then stitch the results into the value
  // returned by sampleUnitRepository.findById
  public SampleUnit findSampleUnit(UUID id) {
    SampleUnit su = sampleUnitRepository.findById(id);
    su.setSampleAttributes(sampleAttributesRepository.findOne(id));
    return su;
  }

  public SampleAttributes findSampleAttributes(UUID id) {
    return sampleAttributesRepository.findOne(id);
  }

  public SampleUnit findSampleUnitBySampleUnitId(UUID sampleUnitId) {
    return sampleUnitRepository.findById(sampleUnitId);
  }

  public List<SampleUnit> findSampleUnitsBySampleSummary(UUID sampleSummaryId) {
    SampleSummary ss = sampleSummaryRepository.findById(sampleSummaryId);
    return sampleUnitRepository.findBySampleSummaryFK(ss.getSampleSummaryPK());
  }

  public List<SampleAttributes> findSampleAttributesByPostcode(String postcode) {
    return sampleAttributesRepository.findByPostcode(postcode);
  }

  public List<SampleUnit> findSampleUnitsByPostcode(String postcode) {
    return findSampleAttributesByPostcode(postcode)
        .stream()
        .map(
            attrs -> {
              SampleUnit su = findSampleUnitBySampleUnitId(attrs.getSampleUnitFK());
              su.setSampleAttributes(attrs);
              return su;
            })
        .collect(Collectors.toList());
  }
}
