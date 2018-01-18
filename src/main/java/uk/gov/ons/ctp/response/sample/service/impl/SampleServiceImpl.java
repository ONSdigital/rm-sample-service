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
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterBusiness;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterCensus;
import uk.gov.ons.ctp.response.sample.ingest.CsvIngesterSocial;
import uk.gov.ons.ctp.response.sample.message.EventPublisher;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnit;
import validation.SampleUnitBase;
import validation.SurveyBase;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public SampleSummary processSampleSummary(SurveyBase surveySample, List<? extends SampleUnitBase> samplingUnitList)
          throws Exception {
    SampleSummary sampleSummary = createSampleSummary(surveySample);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    Map<String, UUID> sampleUnitIds = saveSampleUnits(samplingUnitList, savedSampleSummary);
    publishToPartyQueue(samplingUnitList, sampleUnitIds, sampleSummary.getId().toString());
    return savedSampleSummary;
  }

  protected SampleSummary createSampleSummary(SurveyBase surveySample) throws ParseException {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);

    sampleSummary.setId(UUID.randomUUID());
    return sampleSummary;
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
  private void publishToPartyQueue(List<? extends SampleUnitBase> samplingUnitList, Map<String, UUID> sampleUnitIds, String sampleSummaryId) throws Exception {
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
    Integer sampleUnitsTotal = initialiseSampleUnitsForCollectionExcerciseCollection(job.getSampleSummaryId());
    if (sampleUnitsTotal != 0) {
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

  @Override public SampleSummary ingest(MultipartFile file, String type) throws Exception {
    switch (type) {
      case "B":
        return csvIngesterBusiness.ingest(file);
      case "census":
        return csvIngesterCensus.ingest(file);
      case "social":
        return csvIngesterSocial.ingest(file);
      default:
        throw new UnsupportedOperationException(String.format("Type %s not implemented", type));
    }
  }

  @Override
  public SampleUnit findSampleUnit(UUID id) {
    return sampleUnitRepository.findById(id);
  }

}
