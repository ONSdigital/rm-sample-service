package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.CensusSampleUnit;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;
import uk.gov.ons.ctp.response.sample.definition.SocialSampleUnit;
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
  public SampleSummary processSampleSummary(SurveyBase surveySampleObject) {

    Timestamp effectiveStartDateTime = new Timestamp(surveySampleObject.getEffectiveStartDateTime()
        .toGregorianCalendar().getTimeInMillis());
    Timestamp effectiveEndDateTime = new Timestamp(surveySampleObject.getEffectiveEndDateTime()
        .toGregorianCalendar().getTimeInMillis());

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setEffectiveStartDateTime(effectiveStartDateTime);
    sampleSummary.setEffectiveEndDateTime(effectiveEndDateTime);
    sampleSummary.setSurveyRef(surveySampleObject.getSurveyRef());
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);

    return sampleSummaryRepository.save(sampleSummary);
  }

  @Override
  public void createandSaveSampleUnits(List<? extends SampleUnitBase> samplingUnitList,
      SampleSummary sampleSummary) {

    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleId(sampleSummary.getSampleId());
      sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
      sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());

      sampleUnitRepository.save(sampleUnit);
    }
  }

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
    SampleSummaryDTO.SampleState newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(), SampleSummaryDTO.SampleEvent.ACTIVATED);
    targetSampleSummary.setState(newState);
    sampleSummaryRepository.saveAndFlush(targetSampleSummary);
    return targetSampleSummary;

  }

  public void sendBusinessToParty(Integer sampleId, List<BusinessSampleUnit> samplingUnitList) {
    int size = samplingUnitList.size();
    int position = 1;
    for (BusinessSampleUnit bsu : samplingUnitList) {
      PartyDTO party = mapperFacade.map(bsu, PartyDTO.class);
      party.setSize(size);
      party.setPostion(position);
      party.setSampleId(sampleId);
      sampleServiceClient.postResource("/party/events", party, PartyDTO.class);
      position++;
    }
  }

  public void sendCensusToParty(Integer sampleId, List<CensusSampleUnit> samplingUnitList) {
    int size = samplingUnitList.size();
    int position = 1;
    for (CensusSampleUnit csu : samplingUnitList) {
      PartyDTO party = mapperFacade.map(csu, PartyDTO.class);
      party.setSize(size);
      party.setPostion(position);
      party.setSampleId(sampleId);
      sampleServiceClient.postResource("/party/events", party, PartyDTO.class);
      position++;
    }
  }

  public void sendSocialToParty(Integer sampleId, List<SocialSampleUnit> samplingUnitList) {
    int size = samplingUnitList.size();
    int position = 1;
    for (SocialSampleUnit ssu : samplingUnitList) {
      PartyDTO party = mapperFacade.map(ssu, PartyDTO.class);
      party.setSize(size);
      party.setPostion(position);
      party.setSampleId(sampleId);
      sampleServiceClient.postResource("/party/events", party, PartyDTO.class);
      position++;
    }
  }

  @Override
  public List<SampleUnit> findSampleUnits(String surveyRef, Timestamp exerciseDateTime) {

    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository
        .findBySurveyRefAndEffectiveStartDateTime(surveyRef, exerciseDateTime);

    List<SampleUnit> listOfSampleUnits = new ArrayList<SampleUnit>();

    for (SampleSummary ss : listOfSampleSummaries) {
      SampleUnit su = sampleUnitRepository.findBySampleId(ss.getSampleId());
      listOfSampleUnits.add(su);
    }

    return listOfSampleUnits;
  }

}
