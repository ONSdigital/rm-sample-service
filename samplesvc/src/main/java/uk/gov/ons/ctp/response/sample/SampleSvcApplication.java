package uk.gov.ons.ctp.response.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.state.SampleSvcStateTransitionManagerFactory;

/**
 * The main entry point into the Sample Service SpringBoot Application.
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@ComponentScan(basePackages = {"uk.gov.ons.ctp.response"})
@ImportResource("springintegration/main.xml")
public class SampleSvcApplication {

  @Autowired
  private StateTransitionManagerFactory stateTransitionManager;

  @Autowired
  private AppConfig appConfig;

  /**
   * The SampleService client bean
   * @return the RestClient for the SampleService
   */
  @Bean
  @Qualifier("sampleServiceClient")
  public RestClient sampleServiceClient() {
    RestClient restHelper = new RestClient(appConfig.getSampleSvc().getConnectionConfig());
    return restHelper;
  }
  /**
   * The SampleService client bean
   * @return the RestClient for the SampleService
   */
  @Bean
  @Qualifier("partyServiceClient")
  public RestClient partyServiceClient() {
    RestClient restHelper = new RestClient(appConfig.getPartySvc().getConnectionConfig());
    return restHelper;
  }

  /**
   * Bean to allow application to make controlled state transitions of Samples
   * @return the state transition manager specifically for Samples
   */
  @Bean
  @Qualifier("sampleSummaryTM")
  public StateTransitionManager<SampleState, SampleEvent> sampleStateTransitionManager() {
    return stateTransitionManager.getStateTransitionManager(SampleSvcStateTransitionManagerFactory.SAMPLE_ENTITY);
  }

  /**
   * Bean to allow application to make controlled state transitions of Sample Units
   * @return the state transition manager specifically for Sample Units
   */
  @Bean
  @Qualifier("sampleUnitTM")
  public StateTransitionManager<SampleUnitState, SampleUnitEvent> sampleUnitStateTransitionManager() {
    return stateTransitionManager.getStateTransitionManager(SampleSvcStateTransitionManagerFactory.SAMPLE_UNIT_ENTITY);
  }
  

  @Bean
  public RestExceptionHandler restExceptionHandler() {
    return new RestExceptionHandler();
  }


  @Bean @Primary
  public CustomObjectMapper CustomObjectMapper() {
    return new CustomObjectMapper();
  }
  
  /**
   * This method is the entry point to the Spring Boot application.
   *
   * @param args These are the optional command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(SampleSvcApplication.class, args);
  }
}
