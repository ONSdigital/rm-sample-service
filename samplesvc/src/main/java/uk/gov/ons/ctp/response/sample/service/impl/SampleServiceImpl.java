package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

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
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * Accept feedback from handlers
 */
@Slf4j
@Service
@Configuration
public class SampleServiceImpl implements SampleService {
  @Autowired
  private SampleSummaryRepository sampleSummaryRepository;

  @Autowired
  private SampleUnitRepository sampleUnitRepository;

  @Autowired
  private PartyPublisher sendQueue;

  @Autowired
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent> sampleUnitStateTransitionManager;

  @Autowired
  @Qualifier("sampleServiceClient")
  private RestClient sampleServiceClient;

  @Autowired
  private CollectionExerciseJobService collectionExerciseJobService;


  @Override
  public void processSampleSummary(SurveyBase surveySample, List<? extends SampleUnitBase> samplingUnitList) throws Exception {
    Timestamp effectiveStartDateTime = new Timestamp(surveySample.getEffectiveStartDateTime().toGregorianCalendar().getTimeInMillis());
    Timestamp effectiveEndDateTime = new Timestamp(surveySample.getEffectiveEndDateTime().toGregorianCalendar().getTimeInMillis());

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setEffectiveStartDateTime(effectiveStartDateTime);
    sampleSummary.setEffectiveEndDateTime(effectiveEndDateTime);
    sampleSummary.setSurveyRef(surveySample.getSurveyRef());
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);

    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);

    createAndSaveSampleUnits(samplingUnitList, savedSampleSummary);
    sendToPartyQueue(savedSampleSummary.getSampleSummaryPK(), samplingUnitList);
    sendToPartyService(savedSampleSummary.getSampleSummaryPK(), samplingUnitList);
  }

  /**
   * Effect a state transition for the target SampleSummary if the one is
   * required
   *
   * @param sampleUnitPK the sampleUnitPK to be updated
   * @return SampleSummary the updated SampleSummary
   */


  /**
   * create sampleUnits and save them to the Database
   *
   * @param sampleSummary summary to be saved as sampleUnit
   * @param samplingUnitList list of samplingUnits to be saved
   */
  private void createAndSaveSampleUnits(List<? extends SampleUnitBase> samplingUnitList, SampleSummary sampleSummary) {
    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleSummaryFK(sampleSummary.getSampleSummaryPK());
      sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
      sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());
      sampleUnit.setFormType(sampleUnitBase.getFormType());
      sampleUnit.setState(SampleUnitDTO.SampleUnitState.INIT);

      sampleUnitRepository.save(sampleUnit);
    }
  }

  /**
   * Search for SampleSummary by sampleSummaryPK
   *
   * @param sampleSummaryPK the sampleSummaryPK to be searched for
   * @return SampleSummary matching SampleSummary
   */
  @Override
  public SampleSummary findSampleSummaryBySampleSummaryPK(Integer sampleSummaryPK) {
    return sampleSummaryRepository.findOne(sampleSummaryPK);
  }

  /**
   * Effect a state transition for the target SampleSummary if the one is
   * required
   *
   * @param sampleSummaryPK the sampleSummaryPK to be updated
   * @return SampleSummary the updated SampleSummary
   */
  @Override
  public SampleSummary activateSampleSummaryState(Integer sampleSummaryPK) {
    SampleSummary targetSampleSummary = sampleSummaryRepository.findOne(sampleSummaryPK);
    SampleSummaryDTO.SampleState newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(), SampleSummaryDTO.SampleEvent.ACTIVATED);
    targetSampleSummary.setState(newState);
    sampleSummaryRepository.saveAndFlush(targetSampleSummary);
    return targetSampleSummary;
  }

  /**
   * Send samplingUnits to the queue
   *
   * @param sampleKey the sampleKey of the sample to be sent
   * @param samplingUnitList list of sampling units to be sent
   * @throws Exception
   */
  private void sendToPartyQueue(Integer sampleKey, List<? extends SampleUnitBase> samplingUnitList) throws Exception {
    int size = samplingUnitList.size();
    int position = 1;
    log.debug("Send sampleSummaryFK: {} to Party queue", sampleKey);
    for (SampleUnitBase sub : samplingUnitList) {
      Party party = PartyUtil.convertToParty(sub);
      party.setSize(size);
      party.setPosition(position);
      sendQueue.publish(party);
      position++;
    }
  }

  /**
   * Send samplingUnits to the party service
   *
   * @param sampleKey the sampleKey of the sample to be sent
   * @param samplingUnitList list of sampling units to be sent
   * @throws Exception
   */
  private void sendToPartyService(Integer sampleKey, List<? extends SampleUnitBase> samplingUnitList) throws Exception {
    int size = samplingUnitList.size();
    int position = 1;
    log.debug("Send to party svc");
    for (SampleUnitBase bsu : samplingUnitList) {
      Party party = PartyUtil.convertToParty(bsu);
      party.setSize(size);
      party.setPosition(position);
      sampleServiceClient.postResource("/party/events", party, Party.class);
      position++;
    }
    activateSampleSummaryState(sampleKey);
  }

  /**
   * Save CollectionExerciseJob to collectionExerciseJob table
   *
   * @param collectionExerciseJobCreationRequestDTO
   *          CollectionExerciseJobCreationRequestDTO related to SampleUnits
   * @return Integer Returns sampleUnitsTotal value
   * @throws CTPException if update operation fails or CollectionExerciseJob
   *           already exists
   */
  @Override
  public Integer initialiseCollectionExerciseJob(CollectionExerciseJob job) throws CTPException {
    Integer sampleUnitsTotal = initialiseSampleUnitsForCollectionExcerciseCollection(job.getSurveyRef(), job.getExerciseDateTime());
    if (sampleUnitsTotal != 0) {
      collectionExerciseJobService.storeCollectionExerciseJob(job);
    }
    return sampleUnitsTotal;
  }

  /**
   * Find sampleUnits associated to SampleSummary surveyRef and
   * exerciseDateTime, initialises them and returns the number of sample units
   *
   * @param surveyRef surveyRef to which SampleUnits are related
   * @param exerciseDateTime exerciseDateTime to which SampleUnits are related
   * @return Integer Returns sampleUnitsTotal value
   */
  //TODO: Should we use JPA Batch save to increase performance/ limit network traffic
  //or use an update query. Let Performance Testing prove this first.
  public Integer initialiseSampleUnitsForCollectionExcerciseCollection(String surveyRef, Timestamp exerciseDateTime) {
    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository
        .findBySurveyRefAndEffectiveStartDateTimeAndState(surveyRef, exerciseDateTime, SampleSummaryDTO.SampleState.ACTIVE);

    Integer sampleUnitsTotal = 0;
    for (SampleSummary ss : listOfSampleSummaries) {
      List<SampleUnit> sampleUnitList = sampleUnitRepository.findBySampleSummaryFK(ss.getSampleSummaryPK());
      for (SampleUnit su : sampleUnitList) {
        su.setState(SampleUnitDTO.SampleUnitState.INIT);
        sampleUnitRepository.saveAndFlush(su); 
      }
      sampleUnitsTotal = sampleUnitsTotal + sampleUnitList.size();
    }
    log.debug("sampleUnits found for surveyref : {} {}", surveyRef, sampleUnitsTotal);
    return sampleUnitsTotal;
  }

}
