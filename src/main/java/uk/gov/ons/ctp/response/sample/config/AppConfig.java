package uk.gov.ons.ctp.response.sample.config;

import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * The apps main holder for centralized config read from application.yml or env
 * vars
 *
 */
@CoverageIgnore
@Configuration
@ConfigurationProperties
@Data
public class AppConfig {

  private PartySvc partySvc;
  private SampleUnitDistribution sampleUnitDistribution;
  private DataGrid dataGrid;
  private SwaggerSettings swaggerSettings;
}
