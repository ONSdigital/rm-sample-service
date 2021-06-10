package uk.gov.ons.ctp.response.sample;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import libs.common.error.RestExceptionHandler;
import libs.common.jackson.CustomObjectMapper;
import libs.common.rest.RestUtility;
import libs.common.state.StateTransitionManager;
import libs.common.state.StateTransitionManagerFactory;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.state.SampleSvcStateTransitionManagerFactory;

/** The main entry point into the Sample Service SpringBoot Application. */
@CoverageIgnore
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@IntegrationComponentScan
@ComponentScan(basePackages = {"uk.gov.ons.ctp.response"})
@EnableJpaRepositories(basePackages = {"uk.gov.ons.ctp.response"})
@EntityScan("uk.gov.ons.ctp.response")
@EnableAsync
public class SampleSvcApplication {

  @Autowired private StateTransitionManagerFactory stateTransitionManager;

  @Autowired private AppConfig appConfig;

  /**
   * This method is the entry point to the Spring Boot application.
   *
   * @param args These are the optional command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(SampleSvcApplication.class, args);
  }

  /**
   * The restTemplate bean injected in REST client classes
   *
   * @return the restTemplate used in REST calls
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * The RestUtility bean for the Action service
   *
   * @return the RestUtility bean for the Action service
   */
  @Bean
  @Qualifier("partyRestUtility")
  public RestUtility partyServiceRestUtility() {
    RestUtility restUtility = new RestUtility(appConfig.getPartySvc().getConnectionConfig());
    return restUtility;
  }

  /**
   * Bean to allow application to make controlled state transitions of Samples
   *
   * @return the state transition manager specifically for Samples
   */
  @Bean
  @Qualifier("sampleSummaryTransitionManager")
  public StateTransitionManager<SampleState, SampleEvent> sampleStateTransitionManager() {
    return stateTransitionManager.getStateTransitionManager(
        SampleSvcStateTransitionManagerFactory.SAMPLE_ENTITY);
  }

  /**
   * Bean to allow application to make controlled state transitions of Sample Units
   *
   * @return the state transition manager specifically for Sample Units
   */
  @Bean
  @Qualifier("sampleUnitTransitionManager")
  public StateTransitionManager<SampleUnitState, SampleUnitEvent>
      sampleUnitStateTransitionManager() {
    return stateTransitionManager.getStateTransitionManager(
        SampleSvcStateTransitionManagerFactory.SAMPLE_UNIT_ENTITY);
  }

  /**
   * Rest Exception Handler
   *
   * @return a Rest Exception Handler
   */
  @Bean
  public RestExceptionHandler restExceptionHandler() {
    return new RestExceptionHandler();
  }

  /**
   * Custom Object Mapper
   *
   * @return a customer object mapper
   */
  @Bean
  @Primary
  public CustomObjectMapper customObjectMapper() {
    return new CustomObjectMapper();
  }

  @Bean
  public Validator csvIngestValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }
}
