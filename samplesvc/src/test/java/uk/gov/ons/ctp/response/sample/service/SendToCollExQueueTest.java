package uk.gov.ons.ctp.response.sample.service;

import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.Rabbitmq;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.service.impl.SampleServiceImpl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wardlk on 02/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)

public class SendToCollExQueueTest {

    @InjectMocks
    SampleServiceImpl sampleService;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Mock
    private SampleUnitRepository sampleUnitRepository;

    @Mock
    private CollectionExerciseJobRepository collExJobRepo;

    @Mock
    private SampleUnitPublisher sampleUnitPublisher;

    @Mock
    private AppConfig appConfig;

    @Mock
    private MapperFacade mapperFacade;

    @Test
    public void verifySendingSampleUnitsToQueueAddsThemToQueueAndChangesStateToDelivered(){
        StateTransitionManager stm = mock(StateTransitionManager.class);
        sampleService.setSampleUnitStateTransitionManager(stm);

        when(collExJobRepo.findAll()).thenReturn(Collections.singletonList(new CollectionExerciseJob(1,"str1234",new Timestamp(0),new Timestamp(0))));
        SampleUnit su1 = SampleUnit.builder().sampleId(1).sampleUnitId(2).sampleUnitRef("str1234").sampleUnitType("H").state(SampleUnitDTO.SampleUnitState.INIT).build();
        SampleUnit su2 = SampleUnit.builder().sampleUnitId(3).build();
        List<SampleUnit> suList = new ArrayList<>();
        suList.add(su1);
        suList.add(su2);
        when(sampleUnitRepository.getSampleUnitBatch(any(),any(),any(),any())).thenReturn(suList);
        when(sampleUnitRepository.findOne(2)).thenReturn(su1);
        when(sampleUnitRepository.findOne(3)).thenReturn(su2);
        Rabbitmq rabbitmq = new Rabbitmq();
        rabbitmq.setCount(2);
        when(appConfig.getRabbitmq()).thenReturn(rabbitmq);
        when(mapperFacade.map(any(),any())).thenReturn(new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit());
        when(stm.transition(any(),any())).thenReturn(SampleUnitDTO.SampleUnitState.DELIVERED);

        sampleService.sendSampleUnitsToQueue();

        verify(sampleUnitPublisher, times(2)).send(any());

    }

}
