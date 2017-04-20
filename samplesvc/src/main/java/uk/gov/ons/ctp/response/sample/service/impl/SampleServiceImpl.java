package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SurveyBase;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
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
  private StateTransitionManager<SampleSummaryDTO.SampleState,
      SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Inject
  private MapperFacade mapperFacade;

  @Inject
  @Qualifier("sampleServiceClient")
  private RestClient sampleServiceClient;

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
   * Send samplingUnits to the party service
   *
   * @param sampleId the sampleId of the sample to be sent
   * @param samplingUnitList list of sampling units to be sent
   */
  private void sendToParty(Integer sampleId, List<? extends SampleUnitBase> samplingUnitList) {
    int size = samplingUnitList.size();
    int position = 1;
    for (SampleUnitBase bsu : samplingUnitList) {
      PartyDTO party = mapperFacade.map(bsu, PartyDTO.class);
      party.setSize(size);
      party.setPostion(position);
      party.setSampleId(sampleId);
      sampleServiceClient.postResource("/party/events", party, PartyDTO.class);
      position++;
    }
  }

  /**
   * Find sample units by exercise start date and surveyRef
   *
   * @param exerciseDateTime dateTime to search for
   * @param surveyRef surveyRef to search for
   * @return listOfSampleUnits list of sample units
   */
  @Override
  public List<SampleUnit> findSampleUnits(String surveyRef, Timestamp exerciseDateTime) {

    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository
        .findBySurveyRefAndEffectiveStartDateTimeAndState(surveyRef, exerciseDateTime, SampleSummaryDTO.SampleState.ACTIVE);

    List<SampleUnit> listOfSampleUnits = new ArrayList<SampleUnit>();

    for (SampleSummary ss : listOfSampleSummaries) {
      List<SampleUnit> su = sampleUnitRepository.findBySampleId(ss.getSampleId());
      listOfSampleUnits.addAll(su);
    }

    return listOfSampleUnits;
  }
  
  /**
   * Find sample units by exercise start date and surveyRef
   *
   * @param exerciseDateTime dateTime to search for
   * @param surveyRef surveyRef to search for
   * @return listOfSampleUnits list of sample units
   */
  @Override
  public Integer findSampleUnitsSize(String surveyRef, Timestamp exerciseDateTime) {

    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository
        .findBySurveyRefAndEffectiveStartDateTimeAndState(surveyRef, exerciseDateTime, SampleSummaryDTO.SampleState.ACTIVE);

    Integer sampleUnitsTotal = 0;
    
    for (SampleSummary ss : listOfSampleSummaries) {      
      sampleUnitsTotal = sampleUnitsTotal + sampleUnitRepository.countBySampleId(ss.getSampleId());
    }

    log.debug("sampleUnits: {}", sampleUnitsTotal);
    
    return sampleUnitsTotal;
  }

}
