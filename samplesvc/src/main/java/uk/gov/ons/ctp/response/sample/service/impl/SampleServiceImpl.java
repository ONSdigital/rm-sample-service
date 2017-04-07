package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
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
      activateSampleSummaryState(sampleSummary.getSampleId());
    }
  }
  
  @Override
  public SampleSummary findSampleSummaryBySampleId(Integer sampleId) {
    return sampleSummaryRepository.findBySampleId(sampleId);
  }
  
  /**
   * Effect a state transition for the target case if the category indicates one
   * is required If a transition was made and the state changes as a result,
   * notify the action service of the state change AND if the event was type
   * DISABLED then also call the IAC service to disable/deactivate the IAC code
   * related to the target case.
   * 
   * @param category the category details of the event
   * @param targetCase the 'source' case the event is being created for
   */
  @Override
  public void activateSampleSummaryState(Integer sampleId) {
    
   SampleSummary targetSampleSummary = sampleSummaryRepository.findOne(sampleId );
  
   SampleSummaryDTO.SampleState oldState = targetSampleSummary.getState();
   SampleSummaryDTO.SampleState newState = null;
   // make the transition
   newState = sampleSvcStateTransitionManager.transition(targetSampleSummary.getState(), SampleSummaryDTO.SampleEvent.ACTIVATED);
   // was a state change effected?
   if (oldState != newState) {
     targetSampleSummary.setState(newState);
     sampleSummaryRepository.saveAndFlush(targetSampleSummary);
     //notificationPublisher.sendNotifications(Arrays.asList(prepareCaseNotification(targetCase, transitionEvent)));
   }
  }
  
  @Override
  public void sendToParty(BusinessSurveySample businessSurveySample) {
    List<BusinessSampleUnit> samplingUnitList = businessSurveySample.getSampleUnits().getBusinessSampleUnits();
    for (BusinessSampleUnit bsu : samplingUnitList) {
//      PartyDTO party = new PartyDTO(bsu.getSampleUnitRef(),bsu.getSampleUnitType(), bsu.getForename());
      PartyDTO party = mapperFacade.map(bsu, PartyDTO.class);
      sampleServiceClient.postResource("/party/events", party, PartyDTO.class);
    }
  }
}
