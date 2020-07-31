package uk.gov.ons.ctp.response.sample.scheduled.distribution;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.UUID;
import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.sample.SampleBeanMapper;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;

@RunWith(MockitoJUnitRunner.class)
public class SampleUnitMapperTest {

  @Spy private MapperFacade mapperFacade = new SampleBeanMapper();

  @InjectMocks private SampleUnitMapper sampleUnitMapper;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testMapSuccessWithAttributes() {
    UUID sampleUnitId = UUID.randomUUID();
    UUID collexID = UUID.randomUUID();
    String attributeKey = "ABC_IT_IS_A_KEY";
    String attributeValue = "Here be valuez";

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
        sampleUnitMapper.mapSampleUnit(sampleUnit, collexID.toString());

    assertEquals(sampleUnit.getId().toString(), mappedSampleUnit.getId());
    assertEquals(collexID.toString(), mappedSampleUnit.getCollectionExerciseId());
  }

  @Test
  public void testMapSuccessWithoutAttributes() {
    UUID sampleUnitId = UUID.randomUUID();
    UUID collexID = UUID.randomUUID();

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);

    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit mappedSampleUnit =
        sampleUnitMapper.mapSampleUnit(sampleUnit, collexID.toString());

    assertEquals(sampleUnit.getId().toString(), mappedSampleUnit.getId());
    assertEquals(collexID.toString(), mappedSampleUnit.getCollectionExerciseId());
  }
}
