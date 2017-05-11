package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.sleuth.Tracer;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.config.SampleUnitDistribution;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.scheduled.distribution.SampleUnitDistributor;

/**
 * Created by wardlk on 02/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SendToCollExQueueTest {

    @InjectMocks
    SampleUnitDistributor distributor;

    @Mock
    private SampleUnitRepository sampleUnitRepository;

    @Mock
    private CollectionExerciseJobRepository collectionExerciseJobRepository;

    @Mock
    private SampleUnitPublisher sampleUnitPublisher;

    @Mock
    private AppConfig appConfig;

    @Mock
    private MapperFacade mapperFacade;
    
    @Mock
    private StateTransitionManager sampleUnitStateTransitionManager;
    
    @Mock
    private Tracer tracer;
    
    @Mock
    private DistributedListManager<Integer> sampleUnitDistributionListManager;
    
    
    @Before
    public void setUp() throws Exception {
      MockitoAnnotations.initMocks(this);
    }

    
    @Test
    public void verifySendingSampleUnitsToQueueAddsThemToQueueAndChangesStateToDelivered() throws Exception{
        when(collectionExerciseJobRepository.findAll()).thenReturn(Collections.singletonList(new CollectionExerciseJob(1,"str1234",new Timestamp(0),new Timestamp(0))));
        SampleUnit su1 = SampleUnit.builder().sampleId(1).sampleUnitId(2).sampleUnitRef("str1234").sampleUnitType("H").state(SampleUnitDTO.SampleUnitState.INIT).build();
        SampleUnit su2 = SampleUnit.builder().sampleUnitId(3).build();
        List<SampleUnit> suList = new ArrayList<>();
        suList.add(su1);
        suList.add(su2);
        when(sampleUnitRepository.getSampleUnitBatch(any(),any(),any(),any(),any())).thenReturn(suList);
        when(sampleUnitRepository.findOne(2)).thenReturn(su1);
        when(sampleUnitRepository.findOne(3)).thenReturn(su2);
        SampleUnitDistribution sud = new SampleUnitDistribution();
        sud.setRetrievalMax(2);
        when(appConfig.getSampleDistribution()).thenReturn(sud);
        when(mapperFacade.map(any(),any())).thenReturn(new uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit());
        when(sampleUnitStateTransitionManager.transition(any(),any())).thenReturn(SampleUnitDTO.SampleUnitState.DELIVERED);

        when(tracer.createSpan(any())).thenReturn(null);

        when(sampleUnitDistributionListManager.findList(anyString(), anyBoolean())).thenReturn(new ArrayList<Integer>());
        
        distributor.distribute();

        verify(sampleUnitPublisher, times(2)).send(any());
       
    }

}
