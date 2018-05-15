package uk.gov.ons.ctp.response.sample.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterBusiness;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterCensus;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterSocial;
import uk.gov.ons.ctp.response.sample.message.EventPublisher;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.message.SampleOutboundPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SampleUnitBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@Configuration
public class SampleServiceImpl implements SampleService {

  private static final int NUM_UPLOAD_THREADS = 5;
  private static final int MAX_DESCRIPTION_LENGTH = 5;

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUM_UPLOAD_THREADS);

  @Autowired
  private SampleSummaryRepository sampleSummaryRepository;

  @Autowired
  private SampleUnitRepository sampleUnitRepository;

  @Autowired
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent>
          sampleSvcStateTransitionManager;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
          sampleSvcUnitStateTransitionManager;

  @Autowired
  private PartySvcClientService partySvcClient;

  @Autowired
  private PartyPublisher partyPublisher;

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

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public SampleSummary processSampleSummary(SampleSummary sampleSummary, List<? extends SampleUnitBase> samplingUnitList) {
    int expectedCI = calculateExpectedCollectionInstruments(samplingUnitList);

    sampleSummary.setTotalSampleUnits(samplingUnitList.size());
    sampleSummary.setExpectedCollectionInstruments(expectedCI);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    Map<String, UUID> sampleUnitIds = saveSampleUnits(samplingUnitList, savedSampleSummary);
    publishToPartyQueue(samplingUnitList, sampleUnitIds, sampleSummary.getId().toString());
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

    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());

    return sampleSummaryRepository.save(sampleSummary);
  }

  private Map<String, UUID> saveSampleUnits(List<? extends SampleUnitBase> samplingUnitList, SampleSummary sampleSummary) {
    Map<String, UUID> ids = new HashMap<>();
    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleSummaryFK(sampleSummary.getSampleSummaryPK());
      sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
      sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());
      sampleUnit.setFormType(sampleUnitBase.getFormType());
      sampleUnit.setState(SampleUnitDTO.SampleUnitState.INIT);
      sampleUnit.setId(UUID.randomUUID());
      ids.put(sampleUnit.getSampleUnitRef(), sampleUnit.getId());
      eventPublisher.publishEvent("Sample Init");
      sampleUnitRepository.save(sampleUnit);
    }
    return ids;
  }

  /**
   * create sampleUnits, save them to the Database and post to internal queue
   * @param sampleUnitIds 
   * @throws Exception 
   * */
  private void publishToPartyQueue(List<? extends SampleUnitBase> samplingUnitList, Map<String, UUID> sampleUnitIds, String sampleSummaryId){
    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
          PartyCreationRequestDTO party = PartyUtil.convertToParty(sampleUnitBase);
          party.getAttributes().setSampleUnitId(sampleUnitIds.get(sampleUnitBase.getSampleUnitRef()).toString());
          party.setSampleSummaryId(sampleSummaryId);
          partyPublisher.publish(party);
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
    SampleSummaryDTO.SampleState newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(),
            SampleSummaryDTO.SampleEvent.ACTIVATED);
    targetSampleSummary.setState(newState);
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
    SampleUnitDTO.SampleUnitState newState = sampleSvcUnitStateTransitionManager.transition(sampleUnit.getState(),
            SampleUnitDTO.SampleUnitEvent.PERSISTING);
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
        su.setState(SampleUnitDTO.SampleUnitState.PERSISTED);
        sampleUnitRepository.saveAndFlush(su);
      }
      sampleUnitsTotal = sampleUnitsTotal + sampleUnitList.size();
    }

    log.debug("sampleUnits found for sampleSummaryID : {} {}", sampleSummaryId, sampleUnitsTotal);
    return sampleUnitsTotal;
  }

  public SampleSummary initiateIngest(final SampleSummary sampleSummary, final MultipartFile file, final String type) throws Exception {
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
      SampleSummaryDTO.SampleState newState = sampleSvcStateTransitionManager.transition(sampleSummary.getState(),
              SampleSummaryDTO.SampleEvent.FAIL_VALIDATION);
      sampleSummary.setState(newState);
      sampleSummary.setNotes(message);
      return Optional.of(this.sampleSummaryRepository.save(sampleSummary));
    } catch (CTPException e) {
        log.error("Failed to put sample summary {} into FAILED state - {}", sampleSummary.getId(), e);

        return Optional.empty();
    }
  }

  @Override
  public Optional<SampleSummary> failSampleSummary(SampleSummary sampleSummary, Exception exception){
      return failSampleSummary(sampleSummary, exception.getMessage());
  }

  /**
   * Method to kick off a task to ingest a job
   * @param file Multipart File of SurveySample to be used
   * @param type Type of Survey to be used
   * @return a pair containing the newly created SampleSummary and a Future for the long running task
   * @throws CTPException thrown if upload started message cannot be sent
   */
  @Override
  public Pair<SampleSummary, Future<Optional<SampleSummary>>> ingest(final MultipartFile file, final String type) throws CTPException {
    final SampleSummary newSummary = createAndSaveSampleSummary();

    this.sampleOutboundPublisher.sampleUploadStarted(newSummary);

    Callable<Optional<SampleSummary>> callable =() -> {
      try {
        return Optional.of(initiateIngest(newSummary, file, type));
      } catch (Exception e) {
        return failSampleSummary(newSummary, e);
      }
    };

    Future<Optional<SampleSummary>> future = EXECUTOR_SERVICE.submit(callable);

    return Pair.of(newSummary, future);
  }

  @Override
  public SampleUnit findSampleUnit(UUID id) {
    return sampleUnitRepository.findById(id);
  }

}
