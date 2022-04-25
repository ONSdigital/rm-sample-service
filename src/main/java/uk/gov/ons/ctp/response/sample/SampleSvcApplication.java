package uk.gov.ons.ctp.response.sample;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import libs.common.error.RestExceptionHandler;
import libs.common.jackson.CustomObjectMapper;
import libs.common.rest.RestUtility;
import libs.common.state.StateTransitionManager;
import libs.common.state.StateTransitionManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
@EnableScheduling
@Slf4j
public class SampleSvcApplication {

  @Autowired private StateTransitionManagerFactory stateTransitionManager;
  @Autowired private DataSource dataSource;
  @Autowired private AppConfig appConfig;

  @Bean
  public LiquibaseProperties liquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean
  @DependsOn(value = "entityManagerFactory")
  @DependsOnDatabaseInitialization
  public CustomSpringLiquibase liquibase() {
    LiquibaseProperties liquibaseProperties = liquibaseProperties();
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setContexts(liquibaseProperties.getContexts());
    liquibase.setDataSource(getDataSource(liquibaseProperties));
    liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setShouldRun(true);
    liquibase.setLabels(liquibaseProperties.getLabels());
    liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
    return new CustomSpringLiquibase(liquibase);
  }

  private DataSource getDataSource(LiquibaseProperties liquibaseProperties) {
    if (liquibaseProperties.getUrl() == null) {
      return this.dataSource;
    }

    return DataSourceBuilder.create()
        .url(liquibaseProperties.getUrl())
        .username(liquibaseProperties.getUser())
        .password(liquibaseProperties.getPassword())
        .build();
  }

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

  /**
   * The RestUtility bean for the Party service
   *
   * @return the RestUtility bean for the Party service
   */
  @Bean
  @Qualifier("partyRestUtility")
  public RestUtility partyRestUtility() {
    return new RestUtility(appConfig.getPartySvc().getConnectionConfig());
  }

  /**
   * The RestUtility bean for the CollectionInstrument service
   *
   * @return the RestUtility bean for the CollectionInstrument service
   */
  @Bean
  @Qualifier("collectionInstrumentRestUtility")
  public RestUtility collectionInstrumentRestUtility() {
    return new RestUtility(appConfig.getCollectionInstrumentSvc().getConnectionConfig());
  }

  /**
   * The RestUtility bean for the Survey service
   *
   * @return the RestUtility bean for the Survey service
   */
  @Bean
  @Qualifier("surveyRestUtility")
  public RestUtility surveyRestUtility() {
    return new RestUtility(appConfig.getSurveySvc().getConnectionConfig());
  }

  @Bean
  public Validator csvIngestValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }

  /* PubSub / Spring integration configuration */

  @Bean(name = "sampleSummaryActivationChannel")
  public MessageChannel sampleSummaryInputMessageChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter sampleSummaryInboundChannelAdapter(
      @Qualifier("sampleSummaryActivationChannel") MessageChannel messageChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, appConfig.getGcp().getSampleSummaryActivationSubscription());
    adapter.setOutputChannel(messageChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean(name = "collectionExerciseEndActivationChannel")
  public MessageChannel collectionExerciseEndInputMessageChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter collectionExerciseEndInboundChannelAdapter(
      @Qualifier("collectionExerciseEndActivationChannel") MessageChannel messageChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, appConfig.getGcp().getCollectionExerciseEndSubscription());
    adapter.setOutputChannel(messageChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ServiceActivator(inputChannel = "sampleSummaryActivationStatusChannel")
  public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, appConfig.getGcp().getSampleSummaryActivationStatusTopic());
  }

  @MessagingGateway(defaultRequestChannel = "sampleSummaryActivationStatusChannel")
  public interface PubsubOutboundGateway {
    void sendToPubsub(String text);
  }

  @Bean
  @ServiceActivator(inputChannel = "caseNotificationChannel")
  public MessageHandler caseNotificationMessageSender(PubSubTemplate pubsubTemplate) {
    String topicId = appConfig.getGcp().getCaseNotificationTopic();
    log.info("Application started with publisher for sample to case with topic Id {}", topicId);
    return new PubSubMessageHandler(pubsubTemplate, topicId);
  }

  @MessagingGateway(defaultRequestChannel = "caseNotificationChannel")
  public interface PubSubOutboundCaseNotificationGateway {
    void sendToPubSub(String text);
  }

  public static final String COLLECTION_INSTRUMENT_CACHE = "collectioninstruments";

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager(COLLECTION_INSTRUMENT_CACHE);
  }

  @CacheEvict(
      allEntries = true,
      cacheNames = {COLLECTION_INSTRUMENT_CACHE})
  @Scheduled(fixedDelay = 60000)
  public void cacheEvict() {
    /*
     * This is getting rid of the cached entries in case anything's been changed. We
     * imagine
     * that the maximum of a 1 minute delay to seeing changes reflected in the
     * collection
     * instrument service will not cause any issues
     *
     */
  }
}
