package uk.gov.ons.ctp.response.sample.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
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
  public SampleSummary createSampleSummary(SampleSummary sampleSummary) {
    return sampleSummaryRepository.save(sampleSummary);
  }

  @Override
  public SampleUnit createSampleUnit(SampleUnit sampleUnit) {
    return sampleUnitRepository.save(sampleUnit);
  }
  
  @Override
  public SampleSummary findSampleSummaryBySampleId(Integer sampleId) {
    return sampleSummaryRepository.findBySampleId(sampleId);
  }

}
