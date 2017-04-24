package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SendToParty;
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
@Named
public class SampleServiceImpl implements SampleService {

  @Inject
  private SampleSummaryRepository sampleSummaryRepository;

  @Inject
  private SampleUnitRepository sampleUnitRepository;

  @Inject
  private SendToParty sendQueue;
  
  @Inject
  private StateTransitionManager<SampleSummaryDTO.SampleState,
      SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Inject
  private MapperFacade mapperFacade;

  @Inject
  @Qualifier("sampleServiceClient")
  private RestClient sampleServiceClient;

  @Inject
  private CollectionExerciseJobService collectionExerciseJobService;

  @Override
  public void processSampleSummary(SurveyBase surveySample, List<? extends SampleUnitBase> samplingUnitList) {

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
    sendToQueue(savedSampleSummary.getSampleId(), samplingUnitList);
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

  private void sendToQueue(Integer sampleId, List<? extends SampleUnitBase> samplingUnitList) {
		
	//TODO: change to send to queue;	
	log.debug("Send to queue");
    for (SampleUnitBase bsu : samplingUnitList) {
      Party party = mapperFacade.map(bsu, Party.class);
      party.setSampleId(sampleId);
      sendQueue.send(party);
    } 
  }
  
  /**
   * Send samplingUnits to the party service
   *
   * @param sampleId the sampleId of the sample to be sent
   * @param samplingUnitList list of sampling units to be sent
   */
  private void sendToParty(Integer sampleId, List<? extends SampleUnitBase> samplingUnitList) {

	  
	  
	  
	  
	  
	  
	  
	  
    int size = samplingUnitList.size();
    int position = 1;
    for (SampleUnitBase bsu : samplingUnitList) {
      Party party = mapperFacade.map(bsu, Party.class);
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
    Timestamp exerciseDateTime = collectionExerciseJobCreationRequestDTO.getExerciseDateTime();

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

}
