package uk.gov.ons.ctp.response.sample.endpoint;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.sample.service.NoSampleUnitsInSampleSummaryException;
import uk.gov.ons.ctp.response.sample.service.SampleSummaryDistributionService;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;

@RestController
@RequestMapping(value = "/distribute", produces = "application/json")
public class SampleSummaryDistributionEndpoint {

  private static final Logger LOG =
      LoggerFactory.getLogger(SampleSummaryDistributionEndpoint.class);

  @Autowired private SampleSummaryDistributionService sampleSummaryDistributionService;

  /**
   * Distributes all the SampleUnits for a SampleSummary to case. Distributing here means to tell
   * case about all the sampleUnits so cases can be made against each one to track survey completion
   * progress.
   *
   * @param sampleSummaryId The sample summary ID used to find all the sample units to distribute
   * @return A HTTP response
   */
  @RequestMapping(value = "/{sampleSummaryId}", method = RequestMethod.GET)
  public ResponseEntity<Void> distribute(@PathVariable("sampleSummaryId") UUID sampleSummaryId) {

    LOG.info("Distributing sample units to case", kv("sampleSummaryId", sampleSummaryId));
    try {
      sampleSummaryDistributionService.distribute(sampleSummaryId);
    } catch (UnknownSampleSummaryException | NoSampleUnitsInSampleSummaryException e) {
      LOG.error("Something went wrong", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.badRequest().build();
    }

    LOG.info(
        "Successfully distributed sample units to case", kv("sampleSummaryId", sampleSummaryId));
    return ResponseEntity.ok().build();
  }
}
