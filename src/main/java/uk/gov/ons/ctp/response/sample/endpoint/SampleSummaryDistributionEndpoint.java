package uk.gov.ons.ctp.response.sample.endpoint;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@RestController
@RequestMapping(value = "/distribute", produces = "application/json")
public class SampleSummaryDistributionEndpoint {

  private static final Logger LOG =
      LoggerFactory.getLogger(SampleSummaryDistributionEndpoint.class);
  @Autowired private SampleService sampleService;

  @RequestMapping(
      value =
          "/survey/{surveyId}/collection-exercise/{collectionExerciseId}/samplesummary/{sampleSummaryId}",
      method = RequestMethod.GET)
  public ResponseEntity<Void> distribute(
      @PathVariable("surveyId") String surveyId,
      @PathVariable("collectionExerciseId") String collectionExerciseId,
      @PathVariable("sampleSummaryId") UUID sampleSummaryId) {

    LOG.info("Distributing sample units to case", kv("collectionExerciseId", collectionExerciseId));

    List<SampleUnit> sampleUnits = sampleService.findSampleUnitsBySampleSummary(sampleSummaryId);

    if (sampleUnits.isEmpty()) {
      LOG.info(
          "No sample unit groups to distribute for summary",
          kv("sampleSummaryId", sampleSummaryId));
      return ResponseEntity.badRequest().build();
    }

    // Catch errors distributing sample units so that only failing units are stopped
    sampleUnits.forEach(
        sampleUnit -> {
          try {
            sampleService.distributeSampleUnit(collectionExerciseId, sampleUnit);
          } catch (RuntimeException ex) {
            LOG.error("Failed to distribute sample unit", ex);
          }
        });

    return ResponseEntity.ok().build();
  }
}
