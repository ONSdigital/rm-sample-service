package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/** The apps main holder for centralized config read from application.yml or env vars */
@CoverageIgnore
@Configuration
@ConfigurationProperties
@Data
@EnableRetry
public class AppConfig {

  private PartySvc partySvc;
  private CollectionInstrumentSvc collectionInstrumentSvc;
  private SurveySvc surveySvc;
  private CollectionExerciseSvc collectionExerciseSvc;
  private Logging logging;
  private GCP gcp;
}
