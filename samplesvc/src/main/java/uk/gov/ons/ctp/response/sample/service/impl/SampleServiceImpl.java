package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.message.SendToParty;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * Accept feedback from handlers
 */
@Slf4j
@Component
@Configuration
public class SampleServiceImpl implements SampleService {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private CollectionExerciseJobRepository collectionExerciseJobRepository;

  @Autowired
  private SampleSummaryRepository sampleSummaryRepository;

  @Autowired
  private SampleUnitRepository sampleUnitRepository;

  @Autowired
  private SendToParty sendQueue;

  @Autowired
  @Qualifier("sampleSummaryTM")
  private StateTransitionManager<SampleSummaryDTO.SampleState,
          SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Autowired
  @Qualifier("sampleUnitTM")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState,
          SampleUnitDTO.SampleUnitEvent> sampleUnitStateTransitionManager;


  @Autowired
  private SampleUnitPublisher sampleUnitPublisher;

  @Autowired
  private MapperFacade mapperFacade;

  @Autowired
  @Qualifier("sampleServiceClient")
  private RestClient sampleServiceClient;

  @Autowired
  private CollectionExerciseJobService collectionExerciseJobService;

  @Override
  public void processSampleSummary(SurveyBase surveySample, List<? extends SampleUnitBase> samplingUnitList) throws Exception {

    Timestamp effectiveStartDateTime = new Timestamp(surveySample.getEffectiveStartDateTime()
        .toGregorianCalendar().getTimeInMillis());
    Timestamp effectiveEndDateTime = new Timestamp(surveySample.getEffectiveEndDateTime()
        .toGregorianCalendar().getTimeInMillis());

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setEffectiveStartDateTime(effectiveStartDateTime);
    sampleSummary.setEffectiveEndDateTime(effectiveEndDateTime);
    sampleSummary.setSurveyRef(surveySample.getSurveyRef());
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);

    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);

    createAndSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sendToPartyQueue(savedSampleSummary.getSampleId(), samplingUnitList);
    sendToParty(savedSampleSummary.getSampleId(), samplingUnitList);
  }

/**
   * create sampleUnits and save them to the Database
   *
   * @param sampleSummary  summary to be saved as sampleUnit
   * @param samplingUnitList list of samplingUnits to be saved
   */
  private void createAndSaveSampleUnits(List<? extends SampleUnitBase> samplingUnitList,
                                       SampleSummary sampleSummary) {

    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleId(sampleSummary.getSampleId());
      sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
      sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());
      sampleUnit.setFormType("formtype_tbd");
      sampleUnit.setState(SampleUnitDTO.SampleUnitState.INIT);

      sampleUnitRepository.save(sampleUnit);
    }
  }

  /**
   * Search for SampleSummary by sampleID
   *
   * @param sampleId the sampleId to be searched for
   * @return SampleSummary matching SampleSummary
   */
  @Override
  public SampleSummary findSampleSummaryBySampleId(Integer sampleId) {
    return sampleSummaryRepository.findOne(sampleId);
  }

  /**
   * Effect a state transition for the target SampleSummary if the one is
   * required
   *
   * @param sampleId the sampleId to be updated
   * @return SampleSummary the updated SampleSummary
   */
  @Override
  public SampleSummary activateSampleSummaryState(Integer sampleId) {
    SampleSummary targetSampleSummary = sampleSummaryRepository.findOne(sampleId);
    SampleSummaryDTO.SampleState newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(),
            SampleSummaryDTO.SampleEvent.ACTIVATED);
    targetSampleSummary.setState(newState);
    sampleSummaryRepository.saveAndFlush(targetSampleSummary);
    return targetSampleSummary;

  }

  /**
   * Send samplingUnits to the queue
   *
   * @param sampleId the sampleId of the sample to be sent
   * @param samplingUnitList list of sampling units to be sent
 * @throws Exception 
   */
  private void sendToPartyQueue(Integer sampleId, List<? extends SampleUnitBase> samplingUnitList) throws Exception {
    int size = samplingUnitList.size();
    int position = 1;
    log.debug("Send to queue");
    for (SampleUnitBase bsu : samplingUnitList) {
      Party party = PartyUtil.convertToParty(bsu);
      party.setSize(size);
      party.setPosition(position);
      party.setSampleId(sampleId);
      sendQueue.send(party);
      position++;
    }
  }

  /**
   * Send samplingUnits to the party service
   *
   * @param sampleId the sampleId of the sample to be sent
   * @param samplingUnitList list of sampling units to be sent
 * @throws Exception 
   */
  private void sendToParty(Integer sampleId, List<? extends SampleUnitBase> samplingUnitList) throws Exception {
    int size = samplingUnitList.size();
    int position = 1;
    log.debug("Send to party svc");
    for (SampleUnitBase bsu : samplingUnitList) {
      Party party = PartyUtil.convertToParty(bsu);
      party.setSize(size);
      party.setPosition(position);
      party.setSampleId(sampleId);
      sampleServiceClient.postResource("/party/events", party, Party.class);
      position++;
    }
    activateSampleSummaryState(sampleId);
   }

  /**
   * Save CollectionExerciseJob to collectionExerciseJob table
   *
   * @param collectionExerciseJobCreationRequestDTO CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Integer Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob already exists
   */
  @Override
  public Integer initialiseCollectionExerciseJob(
      CollectionExerciseJobCreationRequestDTO collectionExerciseJobCreationRequestDTO)
          throws CTPException {

    String surveyRef = collectionExerciseJobCreationRequestDTO.getSurveyRef();
    Integer collectionExerciseId = collectionExerciseJobCreationRequestDTO.getCollectionExerciseId();
    Timestamp exerciseDateTime = new Timestamp(collectionExerciseJobCreationRequestDTO.getExerciseDateTime().getTime());

    Integer sampleUnitsTotal = findSampleUnitsSize(surveyRef, exerciseDateTime);

    SampleUnitsRequestDTO sampleUnitsRequest = new SampleUnitsRequestDTO();
    sampleUnitsRequest.setSampleUnitsTotal(sampleUnitsTotal);

    if (sampleUnitsTotal != 0) {
        CollectionExerciseJob collectionExerciseJob = new CollectionExerciseJob();
        collectionExerciseJob.setCollectionExerciseId(collectionExerciseId);
        collectionExerciseJob.setSurveyRef(surveyRef);
        collectionExerciseJob.setExerciseDateTime(exerciseDateTime);
        collectionExerciseJob.setCreatedDateTime(DateTimeUtil.nowUTC());

        collectionExerciseJobService.processCollectionExerciseJob(collectionExerciseJob);

      }

    return sampleUnitsTotal;

  }

  /**
   * Find total number of sampleUnits associated to SampleSummary surveyRef and exerciseDateTime
   *
   * @param surveyRef surveyRef to which SampleUnits are related
   * @param exerciseDateTime exerciseDateTime to which SampleUnits are related
   * @return Integer Returns sampleUnitsTotal value
   */
  public Integer findSampleUnitsSize(String surveyRef, Timestamp exerciseDateTime) {

    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository
        .findBySurveyRefAndEffectiveStartDateTimeAndState(surveyRef, exerciseDateTime,
            SampleSummaryDTO.SampleState.ACTIVE);

    Integer sampleUnitsTotal = 0;

    for (SampleSummary ss : listOfSampleSummaries) {

      List<SampleUnit> sampleUnitList = sampleUnitRepository.findBySampleId(ss.getSampleId());

      for (SampleUnit su : sampleUnitList) {
        su.setState(SampleUnitDTO.SampleUnitState.INIT);
        sampleUnitRepository.saveAndFlush(su);
      }

      sampleUnitsTotal = sampleUnitsTotal + sampleUnitList.size();
    }

    log.debug("sampleUnits: {}", sampleUnitsTotal);

    return sampleUnitsTotal;
  }

  /**
   * Get and send sample units to collection exercise queue
   *
   */
  @Scheduled(cron = "${rabbitmq.cron}")
  public void sendSampleUnitsToQueue() {

    List<CollectionExerciseJob> jobs = collectionExerciseJobRepository.findAll();

    for (int i = 0; i < jobs.size(); i++) {

      List<SampleUnit> sampleUnits = sampleUnitRepository.getSampleUnitBatch(jobs.get(i).getSurveyRef(),
              jobs.get(i).getExerciseDateTime(), SampleSummaryDTO.SampleState.ACTIVE.toString(),
              appConfig.getRabbitmq().getCount());

      for (SampleUnit sampleUnit : sampleUnits) {
        uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit = mapperFacade.map(sampleUnit,
                uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit.class);
        sampleUnitPublisher.send(mappedSampleUnit);
        activateSampleUnitState(sampleUnit.getSampleUnitId());
      }
    }
  }

  /**
   * Effect a state transition for the target SampleSummary if the one is
   * required
   *
   * @param sampleUnitId the sampleUnitId to be updated
   * @return SampleSummary the updated SampleSummary
   */
  @Override
  public SampleUnit activateSampleUnitState(Integer sampleUnitId) {
    SampleUnit targetSampleUnit = sampleUnitRepository.findOne(sampleUnitId);
    SampleUnitDTO.SampleUnitState newState = sampleUnitStateTransitionManager.transition(targetSampleUnit.getState(),
            SampleUnitDTO.SampleUnitEvent.DELIVERING);
    targetSampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(targetSampleUnit);
    return targetSampleUnit;

  }

  // Used form Test only
  public void setSampleUnitStateTransitionManager(StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent> sampleUnitStateTransitionManager) {
    this.sampleUnitStateTransitionManager = sampleUnitStateTransitionManager;
  }
}
