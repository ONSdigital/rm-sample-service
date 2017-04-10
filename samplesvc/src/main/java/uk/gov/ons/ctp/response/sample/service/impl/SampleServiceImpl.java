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
import uk.gov.ons.ctp.response.sample.definition.BusinessSurveySample;
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
@Named
public class SampleServiceImpl implements SampleService {


  @Inject
  private SampleSummaryRepository sampleSummaryRepository;

  @Inject
  private SampleUnitRepository sampleUnitRepository;

  @Inject
  private StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager;

  @Inject
  private MapperFacade mapperFacade;

  @Inject
  @Qualifier("sampleServiceClient")
  private RestClient sampleServiceClient;

  @Override
  public <T extends SurveyBase> SampleSummary createandSaveSampleSummary(T surveySampleObject) {
    Timestamp effectiveStartDateTime = new Timestamp(surveySampleObject.getEffectiveStartDateTime().toGregorianCalendar().getTimeInMillis());
    Timestamp effectiveEndDateTime = new Timestamp(surveySampleObject.getEffectiveEndDateTime().toGregorianCalendar().getTimeInMillis());

    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setEffectiveStartDateTime(effectiveStartDateTime);
    sampleSummary.setEffectiveEndDateTime(effectiveEndDateTime);
    sampleSummary.setSurveyRef(surveySampleObject.getSurveyRef());
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);

    return sampleSummaryRepository.save(sampleSummary);
  }

  @Override
  public <T extends SampleUnitBase> void createandSaveSampleUnits(List<T> samplingUnitList, SampleSummary sampleSummary) {

    for (T businessSampleUnit : samplingUnitList) {
      SampleUnit sampleUnit = new SampleUnit();
      sampleUnit.setSampleId(sampleSummary.getSampleId());
      sampleUnit.setSampleUnitRef(businessSampleUnit.getSampleUnitRef());
      sampleUnit.setSampleUnitType(businessSampleUnit.getSampleUnitType());

      sampleUnitRepository.save(sampleUnit);
    }
  }

  @Override
  public SampleSummary findSampleSummaryBySampleId(Integer sampleId) {
    return sampleSummaryRepository.findBySampleId(sampleId);
  }

  /**
   * Effect a state transition for the target SampleSummary if the one is required
   *
   * @param sampleId the sampleId to be updated
   * @return SampleSummary the updated SampleSummary
   */
  @Override
  public SampleSummary activateSampleSummaryState(Integer sampleId) {

   SampleSummary targetSampleSummary = sampleSummaryRepository.findOne(sampleId);

   SampleSummaryDTO.SampleState oldState = targetSampleSummary.getState();
   SampleSummaryDTO.SampleState newState = null;
   // make the transition
   newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(), SampleSummaryDTO.SampleEvent.ACTIVATED);
   // was a state change effected?
   if (oldState != newState) {
     targetSampleSummary.setState(newState);
     sampleSummaryRepository.saveAndFlush(targetSampleSummary);
   }

   return targetSampleSummary;

  }

  @Override
  public void sendToParty(Integer sampleId, BusinessSurveySample businessSurveySample) {
    List<BusinessSampleUnit> samplingUnitList = businessSurveySample.getSampleUnits().getBusinessSampleUnits();
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

  @Override
  public List<SampleUnit> findSampleUnitsBySurveyRefandExerciseDateTime(String surveyRef, Timestamp exerciseDateTime) {

    List<SampleSummary> listOfSampleSummaries = sampleSummaryRepository.findBySurveyRefAndEffectiveStartDateTime(surveyRef, exerciseDateTime);

    List<SampleUnit> listOfSampleUnits = new ArrayList<SampleUnit>();

    for (SampleSummary ss : listOfSampleSummaries) {
      SampleUnit su = sampleUnitRepository.findBySampleId(ss.getSampleId());
      listOfSampleUnits.add(su);
    }

    return listOfSampleUnits;
  }

}
