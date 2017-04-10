package uk.gov.ons.ctp.response.sample;

import javax.inject.Named;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import uk.gov.ons.ctp.common.jaxrs.JAXRSRegister;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.party.endpoint.PartyEndpoint;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.endpoint.SampleEndpoint;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.state.SampleSvcStateTransitionManagerFactory;

/**
 * The main entry point into the IAC Service SpringBoot Application.
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@ImportResource("springintegration/main.xml")
public class SampleSvcApplication {

  @Autowired
  private StateTransitionManagerFactory sampleSummarySvcStateTransitionManagerFactory;

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
   * Bean to allow application to make controlled state transitions of Samples
   * @return the state transition manager specifically for Samples
   */
  @Bean
  public StateTransitionManager<SampleSummaryDTO.SampleState, SampleSummaryDTO.SampleEvent> sampleSvcStateTransitionManager() {
    return sampleSummarySvcStateTransitionManagerFactory.getStateTransitionManager(
        SampleSvcStateTransitionManagerFactory.SAMPLE_ENTITY);
  }

  /**
   * To register classes in the JAX-RS world.
   */
  @Named
  public static class JerseyConfig extends ResourceConfig {
    /**
     * Its public constructor.
     */
    public JerseyConfig() {

      JAXRSRegister.listCommonTypes().forEach(t->register(t));
      register(PartyEndpoint.class);
      register(SampleEndpoint.class);
      System.setProperty("ma.glasnost.orika.writeSourceFiles", "false");
      System.setProperty("ma.glasnost.orika.writeClassFiles", "false");
    }
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
