package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.AmqpException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.SampleBeanMapper;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

  @Mock private SampleSummaryRepository sampleSummaryRepository;

  @Mock private SampleUnitRepository sampleUnitRepository;

  @Mock private SampleUnitPublisher sampleUnitPublisher;

  @SuppressWarnings("unused") // Spied on in the test
  @Spy
  private MapperFacade mapperFacade = new SampleBeanMapper();

  @Mock private SampleAttributesRepository sampleAttributesRepository;

  @Mock
  private StateTransitionManager<SampleUnitDTO.SampleUnitState, SampleUnitDTO.SampleUnitEvent>
      sampleUnitStateTransitionManager;

  @InjectMocks SampleUnitDistributor sampleUnitDistributor;

  @Test
  public void testShouldPublishSampleUnitWhenSampleSummaryActive() {
    // Given
    SampleSummary sampleSummary =
        SampleSummary.builder().state(SampleSummaryDTO.SampleState.ACTIVE).build();
    SampleUnit sampleUnit = new SampleUnit();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collexId = UUID.randomUUID();
    given(sampleSummaryRepository.findById(sampleSummaryId)).willReturn(sampleSummary);
    given(sampleUnitRepository.findAll(example(sampleSummary.getSampleSummaryPK())))
        .willReturn(Collections.singletonList(sampleUnit));

    // When
    sampleUnitDistributor.publishSampleUnits(sampleSummaryId, collexId);

    // Then
    uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit sampleUnitMessage =
        new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit();
    sampleUnitMessage.setCollectionExerciseId(collexId.toString());
    verify(sampleUnitPublisher).send(sampleUnitMessage);
  }

  @Test
  public void testShouldTransitionSampleUnitWhenSampleSummaryActive() throws CTPException {
    // Given
    SampleSummary sampleSummary =
        SampleSummary.builder().state(SampleSummaryDTO.SampleState.ACTIVE).build();
    SampleUnit sampleUnit =
        SampleUnit.builder().state(SampleUnitDTO.SampleUnitState.PERSISTED).build();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collexId = UUID.randomUUID();
    given(sampleSummaryRepository.findById(sampleSummaryId)).willReturn(sampleSummary);
    given(sampleUnitRepository.findAll(example(sampleSummary.getSampleSummaryPK())))
        .willReturn(Collections.singletonList(sampleUnit));

    // When
    sampleUnitDistributor.publishSampleUnits(sampleSummaryId, collexId);

    // Then
    verify(sampleUnitStateTransitionManager)
        .transition(
            SampleUnitDTO.SampleUnitState.PERSISTED, SampleUnitDTO.SampleUnitEvent.DELIVERING);
    sampleUnit.setState(SampleUnitDTO.SampleUnitState.DELIVERED);
    verify(sampleUnitRepository).save(Collections.singletonList(sampleUnit));
  }

  @Test
  public void testShouldNotTransitionSampleUnitWhenSampleSummaryNotPersisted() throws CTPException {
    // Given
    SampleSummary sampleSummary =
        SampleSummary.builder().state(SampleSummaryDTO.SampleState.INIT).build();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collexId = UUID.randomUUID();
    given(sampleSummaryRepository.findById(sampleSummaryId)).willReturn(sampleSummary);

    // When
    sampleUnitDistributor.publishSampleUnits(sampleSummaryId, collexId);

    // Then
    verify(sampleUnitPublisher, never()).send(any());
    verify(sampleUnitRepository, never()).save(anyListOf(SampleUnit.class));
    verify(sampleUnitStateTransitionManager, never()).transition(any(), any());
  }

  @Test
  public void testShouldContinuePublishingIfOneSampleFails() throws CTPException {
    // Given
    SampleSummary sampleSummary =
        SampleSummary.builder().state(SampleSummaryDTO.SampleState.ACTIVE).build();
    UUID sampleSummaryId = UUID.randomUUID();
    UUID collexId = UUID.randomUUID();
    given(sampleSummaryRepository.findById(sampleSummaryId)).willReturn(sampleSummary);
    given(sampleUnitRepository.findAll(example(sampleSummary.getSampleSummaryPK())))
        .willReturn(Arrays.asList(new SampleUnit(), new SampleUnit()));
    doThrow(new AmqpException("")).when(sampleUnitPublisher).send(any());

    // When
    sampleUnitDistributor.publishSampleUnits(sampleSummaryId, collexId);

    // Then
    verify(sampleUnitPublisher, times(2)).send(any());
  }

  private Example<SampleUnit> example(Integer sampleSummaryPk) {
    SampleUnit sampleUnit =
        SampleUnit.builder()
            .sampleSummaryFK(sampleSummaryPk)
            .state(SampleUnitDTO.SampleUnitState.PERSISTED)
            .build();
    return Example.of(sampleUnit, ExampleMatcher.matchingAll());
  }
}
