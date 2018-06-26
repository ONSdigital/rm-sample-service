package uk.gov.ons.ctp.response.sample.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
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

/** Created by wardlk on 03/07/2017. */
@RunWith(MockitoJUnitRunner.class)
public class StoreCollectionExerciseJobTest {

  private static final UUID COLLECTIONEXERCISEID =
      UUID.fromString("84867a71-264b-46f0-bf7e-7e19e40a6cb8");

  @InjectMocks private CollectionExerciseJobImpl collectionExerciseJobImpl;

  @Mock private CollectionExerciseJobRepository collectionExerciseJobRepository;

  /**
   * Before the test
   *
   * @throws Exception oops
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test that if no CollectionExerciseJob is found with the same CollectionExerciseId as
   * inputCollectionExerciseJob then inputCollectionExerciseJob will be saved
   *
   * @throws CTPException oops
   */
  @Test
  public void verifyStoreCollectionExerciseJobCalledOnceIfCollextionExerciseDoesNotExist()
      throws CTPException {
    CollectionExerciseJob inputCollectionExerciseJob =
        CollectionExerciseJob.builder().collectionExerciseId(COLLECTIONEXERCISEID).build();
    when(collectionExerciseJobRepository.findByCollectionExerciseId(COLLECTIONEXERCISEID))
        .thenReturn(null);
    collectionExerciseJobImpl.storeCollectionExerciseJob(inputCollectionExerciseJob);
    verify(collectionExerciseJobRepository, times(1)).saveAndFlush(inputCollectionExerciseJob);
  }

  /**
   * Test that if a CollectionExerciseJob already exists with the same CollectionExerciseId as
   * inputCollectionExerciseJob a CTPException is thrown
   *
   * @throws CTPException oops
   */
  @Test(expected = CTPException.class)
  public void verifyExceptionThrownIfCollectionExerciseExists() throws CTPException {
    CollectionExerciseJob inputCollectionExerciseJob =
        CollectionExerciseJob.builder().collectionExerciseId(COLLECTIONEXERCISEID).build();
    CollectionExerciseJob returnedCollectionExerciseJob = new CollectionExerciseJob();
    when(collectionExerciseJobRepository.findByCollectionExerciseId(COLLECTIONEXERCISEID))
        .thenReturn(returnedCollectionExerciseJob);
    collectionExerciseJobImpl.storeCollectionExerciseJob(inputCollectionExerciseJob);
  }
}
