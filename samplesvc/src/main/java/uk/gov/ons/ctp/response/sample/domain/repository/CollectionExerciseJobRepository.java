package uk.gov.ons.ctp.response.sample.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.sample.domain.model.CollectionExerciseJob;

import java.util.UUID;

/**
 * JPA Data Repository needed to persist CollectionExerciseJobs
 */
@Repository
public interface CollectionExerciseJobRepository extends JpaRepository<CollectionExerciseJob, Integer> {

    /**
     * Find CollectionExerciseJob entity by collectionExerciseId
     *
     * @param collectionExerciseId The collectionExerciseId
     * @return CollectionExerciseJob object or null
     */
    CollectionExerciseJob findByCollectionExerciseId(UUID collectionExerciseId);

}
