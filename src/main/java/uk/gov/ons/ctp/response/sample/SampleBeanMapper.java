package uk.gov.ons.ctp.response.sample;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.sample.mapper.SampleUnitMapper;

/** The bean mapper to go from Entity objects to Presentation objects. */
@Primary
@Component
public class SampleBeanMapper extends ConfigurableMapper {

  @Override
  public void configureFactoryBuilder(DefaultMapperFactory.Builder builder) {
    builder.compilerStrategy(new EclipseJdtCompilerStrategy());
  }

  /**
   * This method configures the bean mapper.
   *
   * @param factory the mapper factory
   */
  @Override
  protected final void configure(final MapperFactory factory) {
    factory.registerMapper(new SampleUnitMapper());
  }
}
