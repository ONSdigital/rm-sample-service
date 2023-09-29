package uk.gov.ons.ctp.response.sample.config;

import libs.common.rest.RestUtilityConfig;
import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * App config POJO for CollectionInstrument service access - host/location and endpoint locations
 */
@CoverageIgnore
@Data
public class CollectionExerciseSvc {
    private RestUtilityConfig connectionConfig;
    private String collectionExerciseSampleReadinessRequest;
}
