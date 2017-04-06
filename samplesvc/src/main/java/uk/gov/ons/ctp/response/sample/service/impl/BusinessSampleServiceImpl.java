package uk.gov.ons.ctp.response.sample.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.service.BusinessSampleService;

/**
 * Accept feedback from handlers
 */
@Named
public class BusinessSampleServiceImpl implements BusinessSampleService {


  @Inject
  private SampleSummaryRepository sampleSummaryRepository;

  @Inject
  private SampleUnitRepository sampleUnitRepository;
  
  @Override
  public SampleSummary createSampleSummary(SampleSummary sampleSummary) {
    return sampleSummaryRepository.save(sampleSummary);
  }

  @Override
  public SampleSummary findSampleSummaryBySampleId(Integer sampleId) {
    return sampleSummaryRepository.findBySampleId(sampleId);
  }

  @Override
  public SampleUnit createSampleUnit(SampleUnit sampleUnit) {
    sampleUnitRepository.save(sampleUnit);
    return null;
  }

}
