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
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;

@RunWith(MockitoJUnitRunner.class)
public class SampleUnitMapperTest {
  @Mock private SampleAttributesRepository sampleAttributesRepository;

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

    SampleAttributes sampleAttributes = new SampleAttributes();
    sampleAttributes.setAttributes(Collections.singletonMap(attributeKey, attributeValue));

    when(sampleAttributesRepository.findOne(any(UUID.class))).thenReturn(sampleAttributes);

    uk.gov.ons.ctp.response.libs.SampleUnit mappedSampleUnit =
        sampleUnitMapper.mapSampleUnit(sampleUnit, collexID.toString());

    assertEquals(sampleUnit.getId().toString(), mappedSampleUnit.getId());
    assertEquals(collexID.toString(), mappedSampleUnit.getCollectionExerciseId());
    assertEquals(1, mappedSampleUnit.getSampleAttributes().getEntries().size());
    assertEquals(attributeKey, mappedSampleUnit.getSampleAttributes().getEntries().get(0).getKey());
    assertEquals(
        attributeValue, mappedSampleUnit.getSampleAttributes().getEntries().get(0).getValue());
  }

  @Test
  public void testMapSuccessWithoutAttributes() {
    UUID sampleUnitId = UUID.randomUUID();
    UUID collexID = UUID.randomUUID();

    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setId(sampleUnitId);

    when(sampleAttributesRepository.findOne(any(UUID.class))).thenReturn(null);

    uk.gov.ons.ctp.response.libs.SampleUnit mappedSampleUnit =
        sampleUnitMapper.mapSampleUnit(sampleUnit, collexID.toString());

    assertEquals(sampleUnit.getId().toString(), mappedSampleUnit.getId());
    assertEquals(collexID.toString(), mappedSampleUnit.getCollectionExerciseId());
    assertEquals(null, mappedSampleUnit.getSampleAttributes());
  }
}
