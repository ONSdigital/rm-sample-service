package uk.gov.ons.ctp.response.sample.service;

import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;
import uk.gov.ons.ctp.response.sample.domain.repository.CollectionExerciseJobRepository;
import uk.gov.ons.ctp.response.sample.service.impl.CollectionExerciseJobImpl;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by wardlk on 03/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCollectionExerciseJobTest {

    @InjectMocks
    private CollectionExerciseJobImpl collectionExerciseJobImpl;

    @Mock
    private CollectionExerciseJobRepository collectionExerciseJobRepository;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifyStoreCollectionExerciseJobCalledOnceIfCollextionExerciseDoesNotExist() throws CTPException {
        CollectionExerciseJob inputCollectionExerciseJob = CollectionExerciseJob.builder().collectionExerciseId(UUID.randomUUID()).build();
        when(collectionExerciseJobRepository.findByCollectionExerciseId(any())).thenReturn(null);
        collectionExerciseJobImpl.storeCollectionExerciseJob(inputCollectionExerciseJob);
        verify(collectionExerciseJobRepository, times(1)).saveAndFlush(inputCollectionExerciseJob);
    }

    @Test(expected = CTPException.class)
    public void verifyExceptionThrownIfCollectionExerciseExists() throws CTPException {
        CollectionExerciseJob inputCollectionExerciseJob = CollectionExerciseJob.builder().collectionExerciseId(UUID.randomUUID()).build();
        CollectionExerciseJob returnedCollectionExerciseJob = new CollectionExerciseJob();
        when(collectionExerciseJobRepository.findByCollectionExerciseId(any())).thenReturn(returnedCollectionExerciseJob);
        collectionExerciseJobImpl.storeCollectionExerciseJob(inputCollectionExerciseJob);
    }
}
