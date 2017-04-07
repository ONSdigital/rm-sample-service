package uk.gov.ons.ctp.response.sample.service.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import uk.gov.ons.ctp.common.time.DateTimeUtil;
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

}
