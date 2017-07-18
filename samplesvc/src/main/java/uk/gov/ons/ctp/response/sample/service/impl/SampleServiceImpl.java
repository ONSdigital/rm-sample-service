package uk.gov.ons.ctp.response.sample.service.impl;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.party.definition.Party;
import uk.gov.ons.ctp.response.party.representation.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.PartyPostingPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.CollectionExerciseJobService;
import uk.gov.ons.ctp.response.sample.service.PartySvcClientService;
import uk.gov.ons.ctp.response.sample.service.SampleService;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private AppConfig appConfig;

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
   private PartyPostingPublisher partyPostingPublisher;

  @Autowired
  private CollectionExerciseJobService collectionExerciseJobService;

  @Override
  public void processSampleSummary(SurveyBase surveySample, List<? extends SampleUnitBase> samplingUnitList)
          throws Exception {
    SampleSummary sampleSummary = createSampleSummary(surveySample);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    saveSampleUnits(samplingUnitList, savedSampleSummary);
    publishToPartyQueue(samplingUnitList);
  }

  protected SampleSummary createSampleSummary(SurveyBase surveySample) {
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
    return sampleSummary;
  }

  @CoverageIgnore
  private void saveSampleUnits(List<? extends SampleUnitBase> samplingUnitList, SampleSummary sampleSummary) {
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
     * create sampleUnits, save them to the Database and post to internal queue
     *
     * @param samplingUnitList list of samplingUnits to be saved
     */
  @CoverageIgnore
  @ServiceActivator(outputChannel = "partyPostingChannel")
  private void publishToPartyQueue(List<? extends SampleUnitBase> samplingUnitList) {
    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      try {
        if (sampleUnitBase instanceof  BusinessSampleUnit) {
          Party party = PartyUtil.convertToParty(sampleUnitBase);
          partyPostingPublisher.publish(party);
        }
      } catch (Exception e) {
        log.debug("publish exception", e);
      }
    }
  }

  /**
   * Search for SampleSummary by sampleSummaryPK
   *
   * @param sampleSummaryPK the sampleSummaryPK to be searched for
   * @return SampleSummary matching SampleSummary
   */
  @CoverageIgnore
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

  /**
   * Retrieve parties from internal queue and post to partySvc
   *
   * @param party party picked up from queue
   * @throws Exception exception thrown
   */
  @ServiceActivator(inputChannel = "partyTransformed", adviceChain = "partyRetryAdvice")
  public void sendToPartyService(Party party) throws Exception {
    log.debug("Send to party svc");

    PartyCreationRequestDTO partyCreationRequestDTO = PartyUtil.createPartyCreationRequestDTO(party);
    partyCreationRequestDTO.setSampleUnitRef(party.getSampleUnitRef());
    partyCreationRequestDTO.setSampleUnitType(party.getSampleUnitType());
    Map<String, String> attMap = new HashMap<>();
    attMap.putAll(party.getAttributes());
    partyCreationRequestDTO.setAttributes(attMap);

    PartyDTO returned = partySvcClient.postParty(partyCreationRequestDTO);
    log.info(returned.getId());
    SampleUnit sampleUnit = sampleUnitRepository.findBySampleUnitRefAndType(party.getSampleUnitRef(),
            party.getSampleUnitType());
    changeSampleUnitState(sampleUnit);
    sampleSummaryStateCheck(sampleUnit);
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
      if(total == partied) {
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
    Integer sampleUnitsTotal = initialiseSampleUnitsForCollectionExcerciseCollection(job.getSurveyRef(),
            job.getExerciseDateTime());
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
  //TODO: Should we use JPA Batch save to increase performance/limit network traffic?
  //or use an update query. Let Performance Testing prove this first.
  public Integer initialiseSampleUnitsForCollectionExcerciseCollection(String surveyRef, Timestamp exerciseDateTime) {
    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository
        .findBySurveyRefAndEffectiveStartDateTimeAndState(surveyRef, exerciseDateTime,
                SampleSummaryDTO.SampleState.ACTIVE);

    Integer sampleUnitsTotal = 0;
    for (SampleSummary ss : listOfSampleSummaries) {
      List<SampleUnit> sampleUnitList = sampleUnitRepository.findBySampleSummaryFK(ss.getSampleSummaryPK());
      for (SampleUnit su : sampleUnitList) {
        su.setState(SampleUnitDTO.SampleUnitState.PERSISTED);
        sampleUnitRepository.saveAndFlush(su);
      }
      sampleUnitsTotal = sampleUnitsTotal + sampleUnitList.size();
    }
    log.debug("sampleUnits found for surveyref : {} {}", surveyRef, sampleUnitsTotal);
    return sampleUnitsTotal;
  }

}
