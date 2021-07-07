package uk.gov.ons.ctp.response.sample.endpoint;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.sample.service.UnknownSampleSummaryException;
import uk.gov.ons.ctp.response.sample.service.ValidateSampleSummaryService;

@RestController
@RequestMapping(value = "/validate", produces = "application/json")
public class ValidateSampleSummaryEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(ValidateSampleSummaryEndpoint.class);

  @Autowired private ValidateSampleSummaryService validateSampleSummaryService;

  @RequestMapping(
      value =
          "/survey/{surveyId}/collection-exercise/{collectionExerciseId}/samplesummary/{sampleSummaryId}",
      method = RequestMethod.GET)
  public ResponseEntity<Void> validate(
      @PathVariable("surveyId") String surveyId,
      @PathVariable("collectionExerciseId") String collectionExerciseId,
      @PathVariable("sampleSummaryId") UUID sampleSummaryId) {

    LOG.debug(
        "about to validate sample summary",
        kv("sampleSummaryId", sampleSummaryId),
        kv("surveyId", surveyId),
        kv("collectionExerciseId", collectionExerciseId));

    try {
      boolean validated =
          validateSampleSummaryService.validate(surveyId, sampleSummaryId, collectionExerciseId);
      LOG.debug(
          "Validated sample summary",
          kv("sampleSummaryId", sampleSummaryId),
          kv("surveyId", surveyId),
          kv("collectionExerciseId", collectionExerciseId),
          kv("validated", validated));
      if (validated) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
    } catch (UnknownSampleSummaryException e) {
      LOG.error("unknown sample summary id", kv("sampleSummaryId", sampleSummaryId), e);
      return ResponseEntity.badRequest().build();
    }
  }
}
