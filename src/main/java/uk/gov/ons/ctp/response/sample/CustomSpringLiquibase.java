package uk.gov.ons.ctp.response.sample;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * This class was necessary due to the changes made in Spring Boot 2.5.5, which changed the order of
 * SQL loading. It means we now need to define our own Liquibase integration and set it to run after
 * Hibernate does.
 */
public class CustomSpringLiquibase implements InitializingBean, BeanNameAware, ResourceLoaderAware {
  private SpringLiquibase springLiquibase;

  public CustomSpringLiquibase(SpringLiquibase liquibase) {
    this.springLiquibase = liquibase;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    springLiquibase.afterPropertiesSet();
  }

  @Override
  public void setBeanName(String name) {
    springLiquibase.setBeanName(name);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    springLiquibase.setResourceLoader(resourceLoader);
  }
}
