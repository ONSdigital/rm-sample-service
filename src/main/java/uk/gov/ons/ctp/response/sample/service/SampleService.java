package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.*;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import libs.common.time.DateTimeUtil;
import libs.party.representation.PartyDTO;
import libs.sample.validation.BusinessSampleUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
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

  @Autowired private CollectionExerciseJobService collectionExerciseJobService;

  @Autowired private PartyPublisher partyPublisher;

  public List<SampleSummary> findAllSampleSummaries() {
    return sampleSummaryRepository.findAll();
  }

  public SampleSummary findSampleSummary(UUID id) {
    return sampleSummaryRepository.findById(id).orElse(null);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public SampleSummary saveSample(
      SampleSummary sampleSummary,
      List<BusinessSampleUnit> samplingUnitList,
      SampleUnitState sampleUnitState) {
    int expectedCI = calculateExpectedCollectionInstruments(samplingUnitList);

    sampleSummary.setTotalSampleUnits(samplingUnitList.size());
    sampleSummary.setExpectedCollectionInstruments(expectedCI);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    saveSampleUnits(samplingUnitList, savedSampleSummary, sampleUnitState);

    return savedSampleSummary;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public SampleUnit createSampleUnit(
      UUID sampleSummaryId, BusinessSampleUnit samplingUnit, SampleUnitState sampleUnitState)
      throws UnknownSampleSummaryException {

    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId).orElse(null);
    if (sampleSummary != null) {
      // the csv ingester does this so it's needed here
      samplingUnit.setSampleUnitType("B");

      /** a sample unit should be unique inside a sample summary, so check if we already have it. */
      boolean exists =
          sampleUnitRepository.existsBySampleUnitRefAndSampleSummaryFK(
              samplingUnit.getSampleUnitRef(), sampleSummary.getSampleSummaryPK());
      if (!exists) {
        return createAndSaveSampleUnit(sampleSummary, sampleUnitState, samplingUnit);
      } else {
        throw new IllegalStateException("sample unit already exists");
      }
    } else {
      throw new UnknownSampleSummaryException();
    }
  }

  public void publishSampleToParty(UUID sampleSummaryId, BusinessSampleUnit samplingUnit) {
    PartyCreationRequestDTO party = PartyUtil.convertToParty(samplingUnit);
    party.getAttributes().setSampleUnitId(samplingUnit.getSampleUnitId().toString());
    party.setSampleSummaryId(sampleSummaryId.toString());
    partyPublisher.publish(party);
  }

  private Integer calculateExpectedCollectionInstruments(
      List<BusinessSampleUnit> samplingUnitList) {
    // TODO: get survey classifiers from survey service, currently using formtype for all business
    // surveys
    Set<String> formTypes = new HashSet<>();
    for (BusinessSampleUnit businessSampleUnit : samplingUnitList) {
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

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public SampleSummary createAndSaveSampleSummary(SampleSummaryDTO summaryDTO) {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setState(SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());
    sampleSummary.setTotalSampleUnits(summaryDTO.getTotalSampleUnits());
    sampleSummary.setExpectedCollectionInstruments(summaryDTO.getExpectedCollectionInstruments());
    log.debug("about to save sample summary");
    return sampleSummaryRepository.save(sampleSummary);
  }

  private void saveSampleUnits(
      List<BusinessSampleUnit> samplingUnitList,
      SampleSummary sampleSummary,
      SampleUnitState sampleUnitState) {
    for (BusinessSampleUnit sampleUnitBase : samplingUnitList) {
      createAndSaveSampleUnit(sampleSummary, sampleUnitState, sampleUnitBase);
    }
  }

  private SampleUnit createAndSaveSampleUnit(
      SampleSummary sampleSummary,
      SampleUnitState sampleUnitState,
      BusinessSampleUnit sampleUnitBase) {
    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setSampleSummaryFK(sampleSummary.getSampleSummaryPK());
    sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
    sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());
    sampleUnit.setFormType(sampleUnitBase.getFormType());
    sampleUnit.setState(sampleUnitState);
    sampleUnit.setId(sampleUnitBase.getSampleUnitId());
    sampleUnitRepository.save(sampleUnit);
    return sampleUnit;
  }

  /**
   * Effect a state transition for the target SampleSummary if one is required
   *
   * @param sampleSummaryPK the sampleSummaryPK to be updated
   * @return SampleSummary the updated SampleSummary
   * @throws CTPException if transition errors
   */
  public SampleSummary activateSampleSummaryState(Integer sampleSummaryPK) throws CTPException {
    log.debug("attempting to find sample summary", kv("sampleSummaryPK", sampleSummaryPK));
    try {
      SampleSummary targetSampleSummary =
          sampleSummaryRepository.findBySampleSummaryPK(sampleSummaryPK).orElseThrow();
      SampleState newState =
          sampleSvcStateTransitionManager.transition(
              targetSampleSummary.getState(), SampleEvent.ACTIVATED);
      targetSampleSummary.setState(newState);
      targetSampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
      sampleSummaryRepository.saveAndFlush(targetSampleSummary);
      return targetSampleSummary;
    } catch (NoSuchElementException e) {
      log.error("unable to find sample summary", kv("sampleSummaryPK", sampleSummaryPK));
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND);
    }
  }

  public PartyDTO sendToPartyService(PartyCreationRequestDTO partyCreationRequest)
      throws Exception {
    // PartyDTO returnedParty = partySvcClient.postParty(partyCreationRequest);
    String sampleUnitId = partyCreationRequest.getAttributes().getSampleUnitId();
    try {
      SampleUnit sampleUnit =
          sampleUnitRepository.findById(UUID.fromString(sampleUnitId)).orElseThrow();
      changeSampleUnitState(sampleUnit);
      sampleSummaryStateCheck(sampleUnit);
      //   addPartyIdToSample(sampleUnit, returnedParty);
      //      return returnedParty;
      return null;
    } catch (NoSuchElementException e) {
      log.error("unable to find sample ", kv("sampleUnitId", sampleUnitId));
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND);
    }
  }

  private void addPartyIdToSample(SampleUnit sampleUnit, PartyDTO party) {
    try {
      log.debug(
          "add party to sample",
          kv("sampleUnitId", sampleUnit.getId()),
          kv("partyId", party.getId()));
      UUID partyId = UUID.fromString(party.getId());
      sampleUnit.setPartyId(partyId);
      sampleUnitRepository.saveAndFlush(sampleUnit);
      log.debug(
          "party added", kv("sampleUnitId", sampleUnit.getId()), kv("partyId", party.getId()));
    } catch (RuntimeException e) {
      log.error(
          "Unexpected exception saving party id",
          kv("sampleUnitId", sampleUnit.getId()),
          kv("partyId", party.getId()),
          e);
    }
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
    SampleSummary sampleSummary =
        sampleSummaryRepository.findById(job.getSampleSummaryId()).orElse(null);
    if (sampleSummary != null && sampleSummary.getTotalSampleUnits() != 0) {
      sampleUnitsTotal = sampleSummary.getTotalSampleUnits();
      collectionExerciseJobService.storeCollectionExerciseJob(job);
    }
    return sampleUnitsTotal;
  }

  public int getSampleSummaryUnitCount(UUID sampleSummaryId) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId).orElse(null);
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
          e);

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
    SampleUnit su = sampleUnitRepository.findById(id).orElse(null);
    return su;
  }

  public List<SampleUnit> findSampleUnitsBySampleSummary(UUID sampleSummaryId) {
    try {
      SampleSummary ss = sampleSummaryRepository.findById(sampleSummaryId).orElseThrow();
      return sampleUnitRepository.findBySampleSummaryFK(ss.getSampleSummaryPK());
    } catch (NoSuchElementException e) {
      log.error("unable to find sample summary", kv("sampleSummaryId", sampleSummaryId));
      return new ArrayList<>();
    }
  }
}
