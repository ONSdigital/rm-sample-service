package uk.gov.ons.ctp.response.sample.endpoint;

import libs.common.error.CTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitParentDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryEnrichmentService;

import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
@RequestMapping(value = "/distribute", produces = "application/json")
public class SampleSummaryDistributionEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(SampleSummaryDistributionEndpoint.class);

  @Autowired private SampleSummaryEnrichmentService sampleSummaryEnrichmentService;
  @Autowired private SampleService sampleService;
  @Autowired private SampleUnitPublisher sampleUnitPublisher;

  @RequestMapping(
      value =
          "/survey/{surveyId}/collection-exercise/{collectionExerciseId}/samplesummary/{sampleSummaryId}",
      method = RequestMethod.GET)
  public ResponseEntity<Void> distribute(
      @PathVariable("surveyId") String surveyId,
      @PathVariable("collectionExerciseId") String collectionExerciseId,
      @PathVariable("sampleSummaryId") UUID sampleSummaryId) {

    LOG.info("Distributing sample unit",
             kv("collectionExerciseId", collectionExerciseId));

    // Don't need to get a sample unit group as it's a 1-1 mapping between sample units.  We can just get a list of
    // all the sample units for a sample summary id and go from there.

    List<SampleUnit> sampleUnits = sampleService.findSampleUnitsBySampleSummary(sampleSummaryId);

    if (sampleUnits.isEmpty()) {
      LOG.info("No sample unit groups to distribute for exercise",
              kv("collectionExerciseId", collectionExerciseId));
      return ResponseEntity.badRequest().build();
    }

    // Catch errors distributing sample units so that only failing units are stopped
    sampleUnits.forEach(
            sampleUnit -> {
              try {
                distributeSampleUnitGroup(collectionExerciseId, sampleUnit);
              } catch (CTPException ex) {
                log.with("sampleUnitGroupPK", sampleUnitGroup.getSampleUnitGroupPK())
                        .error("Failed to distribute sample unit group", ex);
              }
            });

    return ResponseEntity.ok().build();
  }

  /**
   * Distribute SampleUnits for a SampleUnitGroup. Will send the sampleUnitParent data to Case via
   * PubSub and will transition the sampleUnitGroup state in collection exercise to PUBLISHED on
   * success.
   *
   * @param collectionExerciseId Collection exercise id for the sample unit
   * @param sampleUnit for which to distribute sample units
   */
  private void distributeSampleUnitGroup(
          String collectionExerciseId, SampleUnit sampleUnit) throws CTPException {
    SampleUnitParentDTO sampleUnitParent;

    boolean activeEnrolment = sampleUnit.isActiveEnrolment();
    sampleUnitParent = sampleUnit.toSampleUnitParent(activeEnrolment, collectionExerciseId);

    publishSampleUnitToCase(sampleUnitParent);
  }

  /**
   * Publish a message to the Case Service for a SampleUnitGroup and transition state. Note this is
   * a transaction boundary but as a private method we cannot use Spring declarative transaction
   * management but must use a programmatic transaction.
   *
   * @param sampleUnitGroup from which publish message created and for which to transition state.
   * @param sampleUnitMessage to publish.
   */
  private void publishSampleUnitToCase(SampleUnitParentDTO sampleUnitMessage) {

    transactionTemplate.execute(
            new TransactionCallbackWithoutResult() {
              // the code in this method executes in a transaction context
              protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                  sampleUnitPublisher.sendSampleUnitToCase(sampleUnitMessage);
                } catch (CTPException ex) {
                  LOG.error("Sample Unit group state transition failed", ex);
                }
              }
            });
  }
}
