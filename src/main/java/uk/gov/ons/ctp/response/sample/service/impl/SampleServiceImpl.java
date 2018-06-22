package uk.gov.ons.ctp.response.sample.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
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
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SampleUnitBase;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Configuration
public class SampleServiceImpl implements SampleService {

  @Autowired
  private SampleSummaryRepository sampleSummaryRepository;

  @Autowired
  private SampleUnitRepository sampleUnitRepository;

  @Autowired
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleState, SampleEvent>
          sampleSvcStateTransitionManager;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitState, SampleUnitEvent>
          sampleSvcUnitStateTransitionManager;

  @Autowired
  private PartySvcClientService partySvcClient;

  @Autowired
  private SampleOutboundPublisher sampleOutboundPublisher;

  @Autowired
  private CollectionExerciseJobService collectionExerciseJobService;

  @Autowired
  private CsvIngesterBusiness csvIngesterBusiness;

  @Autowired
  private CsvIngesterCensus csvIngesterCensus;

  @Autowired
  private CsvIngesterSocial csvIngesterSocial;

  @Override
  public List<SampleSummary> findAllSampleSummaries() {
    return sampleSummaryRepository.findAll();
  }

  @Override
  public SampleSummary findSampleSummary(UUID id) {
    return sampleSummaryRepository.findById(id);
  }
  
  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private SampleAttributesRepository sampleAttributesRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public SampleSummary saveSample(SampleSummary sampleSummary, List<? extends SampleUnitBase> samplingUnitList, SampleUnitState sampleUnitState) {
    int expectedCI = calculateExpectedCollectionInstruments(samplingUnitList);

    sampleSummary.setTotalSampleUnits(samplingUnitList.size());
    sampleSummary.setExpectedCollectionInstruments(expectedCI);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    saveSampleUnits(samplingUnitList, savedSampleSummary, sampleUnitState);

    return savedSampleSummary;
  }

  private Integer calculateExpectedCollectionInstruments(List<? extends SampleUnitBase> samplingUnitList) {
    //TODO: get survey classifiers from survey service, currently using formtype for all business surveys
    Set<String> formTypes = new HashSet<>();
    for (SampleUnitBase businessSampleUnit : samplingUnitList) {
      formTypes.add(businessSampleUnit.getFormType());
    }
    return formTypes.size();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public SampleSummary createAndSaveSampleSummary(){
    SampleSummary sampleSummary = new SampleSummary();

    sampleSummary.setState(SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());

    return sampleSummaryRepository.save(sampleSummary);
  }

  private void saveSampleUnits(List<? extends SampleUnitBase> samplingUnitList, SampleSummary sampleSummary, SampleUnitState sampleUnitState) {
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
  @Override
  public SampleSummary activateSampleSummaryState(Integer sampleSummaryPK) throws CTPException {
    SampleSummary targetSampleSummary = sampleSummaryRepository.findOne(sampleSummaryPK);
    SampleState newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(),
            SampleEvent.ACTIVATED);
    targetSampleSummary.setState(newState);
    targetSampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummaryRepository.saveAndFlush(targetSampleSummary);
    // Notify the outside world the sample upload has finished
    this.sampleOutboundPublisher.sampleUploadFinished(targetSampleSummary);

    return targetSampleSummary;
  }

  @Override
  public PartyDTO sendToPartyService(PartyCreationRequestDTO partyCreationRequest) throws Exception {
    PartyDTO returnedParty = partySvcClient.postParty(partyCreationRequest);
    SampleUnit sampleUnit = sampleUnitRepository.findById(UUID.fromString(partyCreationRequest.getAttributes().getSampleUnitId()));
    changeSampleUnitState(sampleUnit);
    sampleSummaryStateCheck(sampleUnit);
    return returnedParty;
  }

  private void changeSampleUnitState(SampleUnit sampleUnit) throws CTPException {
    SampleUnitState newState = sampleSvcUnitStateTransitionManager.transition(sampleUnit.getState(),
            SampleUnitEvent.PERSISTING);
    sampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(sampleUnit);
  }

  private void sampleSummaryStateCheck(SampleUnit sampleUnit) throws CTPException {
    int partied = sampleUnitRepository.getPartiedForSampleSummary(sampleUnit.getSampleSummaryFK());
    int total = sampleUnitRepository.getTotalForSampleSummary(sampleUnit.getSampleSummaryFK());
    if (total == partied) {
      activateSampleSummaryState(sampleUnit.getSampleSummaryFK());
    }
  }

  /**
   * Save CollectionExerciseJob to collectionExerciseJob table
   *
   * @param job CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Integer Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob
   *           already exists
   */
  @Override
  public Integer initialiseCollectionExerciseJob(CollectionExerciseJob job) throws CTPException {
    //Integer sampleUnitsTotal = initialiseSampleUnitsForCollectionExcerciseCollection(job.getSampleSummaryId());
	  Integer sampleUnitsTotal = 0;
	  SampleSummary sampleSummary = sampleSummaryRepository.findById(job.getSampleSummaryId());
	  if (sampleSummary != null && sampleSummary.getTotalSampleUnits() != 0) {
		  sampleUnitsTotal = sampleSummary.getTotalSampleUnits();
		  collectionExerciseJobService.storeCollectionExerciseJob(job);
    }
    return sampleUnitsTotal;
  }

  /**
   * Find sampleUnits associated to SampleSummary surveyRef and
   * exerciseDateTime, initialises them and returns the number of sample units
   *
   * @param sampleSummaryId Sample Summary ID to which SampleUnits are related
   * @return Integer Returns sampleUnitsTotal value
   */
  //TODO: Should we use JPA Batch save to increase performance/limit network traffic?
  //or use an update query. Let Performance Testing prove this first.
  public Integer initialiseSampleUnitsForCollectionExcerciseCollection(UUID sampleSummaryId) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId);

    Integer sampleUnitsTotal = 0;
    if(sampleSummary != null) {
      List<SampleUnit> sampleUnitList = sampleUnitRepository.findBySampleSummaryFK(sampleSummary.getSampleSummaryPK());
      for (SampleUnit su : sampleUnitList) {
        su.setState(SampleUnitState.PERSISTED);
        sampleUnitRepository.saveAndFlush(su);
      }
      sampleUnitsTotal = sampleUnitsTotal + sampleUnitList.size();
    }

    log.debug("sampleUnits found for sampleSummaryID : {} {}", sampleSummaryId, sampleUnitsTotal);
    return sampleUnitsTotal;
  }

  @Override
  public SampleSummary ingest(final SampleSummary sampleSummary, final MultipartFile file, final String type) throws Exception {
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

  @Override
  public Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, String message){
    try {
      SampleState newState = sampleSvcStateTransitionManager.transition(sampleSummary.getState(),
              SampleEvent.FAIL_VALIDATION);
      sampleSummary.setState(newState);
      sampleSummary.setNotes(message);
      SampleSummary persisted = this.sampleSummaryRepository.save(sampleSummary);

      return Optional.of(persisted);
    } catch (CTPException e) {
        log.error("Failed to put sample summary {} into FAILED state - {}", sampleSummary.getId(), e);

        return Optional.empty();
    } catch(RuntimeException e){
      // Hibernate throws RuntimeException if any issue persisting the SampleSummary.  This is to ensure it is logged
      // (otherwise they just disappear).
      log.error("Failed to persist sample summary - {}", e);

      throw e;
    }
  }

  @Override
  public Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, Exception exception){
      return failSampleSummary(sampleSummary, exception.getMessage());
  }

  // TODO get this to get the attributes in a separate call then stitch the results into the value returned by sampleUnitRepository.findById
  @Override
  public SampleUnit findSampleUnit(UUID id) {
    SampleUnit su = sampleUnitRepository.findById(id);
    su.setSampleAttributes(sampleAttributesRepository.findOne(id));
    return su;
  }

  @Override
  public SampleAttributes findSampleAttributes(UUID id) {
    return sampleAttributesRepository.findOne(id);
  }

  @Override
  public SampleUnit findSampleUnitBySampleUnitRef(String sampleUnitRef) {
    return sampleUnitRepository.findBySampleUnitRef(sampleUnitRef);
  }

  @Override
  public SampleUnit findSampleUnitBySampleUnitId(UUID sampleUnitId) {
    return sampleUnitRepository.findById(sampleUnitId);
  }

  @Override
  public List<SampleUnit> findSampleUnitsBySampleSummary(UUID sampleSummaryId) {
    SampleSummary ss = sampleSummaryRepository.findById(sampleSummaryId);
    return sampleUnitRepository.findBySampleSummaryFK(ss.getSampleSummaryPK());
  }

}
