package uk.gov.ons.ctp.response.sample.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import uk.gov.ons.ctp.response.sample.domain.repository.SampleRepository;
import uk.gov.ons.ctp.response.sample.service.SampleService;

/**
 * Accept feedback from handlers
 */
@Named
public class SampleServiceImpl implements SampleService {


  @Inject
  private SampleRepository sampleRepository;

 
}
