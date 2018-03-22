package uk.gov.ons.ctp.response.sample.ingest;

import liquibase.util.csv.opencsv.CSVReader;
import liquibase.util.csv.opencsv.bean.ColumnPositionMappingStrategy;
import liquibase.util.csv.opencsv.bean.CsvToBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SocialSampleUnit;
import validation.SocialSurveySample;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvIngesterSocial extends CsvToBean<SocialSampleUnit> {

  private static final String SAMPLEUNITREF = "sampleUnitRef";
  private static final String FORMTYPE = "formType";

  private static final String[] COLUMNS = new String[] {SAMPLEUNITREF, FORMTYPE};

  @Autowired
  private SampleService sampleService;

  private ColumnPositionMappingStrategy<SocialSampleUnit> columnPositionMappingStrategy;

  /**
   * Lazy create a reusable validator
   *
   * @return the cached validator
   */
  @Cacheable(cacheNames = "csvIngestValidator")
  private Validator getValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }

  public CsvIngesterSocial() {
    columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
    columnPositionMappingStrategy.setType(SocialSampleUnit.class);
    columnPositionMappingStrategy.setColumnMapping(COLUMNS);
  }

  public SampleSummary ingest(MultipartFile file, String collectionExerciseId)
      throws Exception {

    CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()), ':');
    String[] nextLine;
    SampleSummary sampleSummary;
    SocialSurveySample businessSurveySample = new SocialSurveySample();
    List<SocialSampleUnit> samplingUnitList = new ArrayList<>();
    Integer expectedCI = 1; //TODO: when social surveys are onboarded expectedCI should be calculated

      while((nextLine = csvReader.readNext()) != null) {

          SocialSampleUnit businessSampleUnit = processLine(columnPositionMappingStrategy, nextLine);
          Optional<String> namesOfInvalidColumns = validateLine(businessSampleUnit);
          if (namesOfInvalidColumns.isPresent()) {
            log.error("Problem parsing line {} due to {} - entire ingest aborted", Arrays.toString(nextLine),
                namesOfInvalidColumns.get());
            throw new CTPException(CTPException.Fault.VALIDATION_FAILED, String.format("Problem parsing line %s due to %s", Arrays.toString(nextLine),
                namesOfInvalidColumns.get()));
          }
          
          samplingUnitList.add(businessSampleUnit);

      }

      businessSurveySample.setSampleUnits(samplingUnitList);

      sampleSummary = sampleService.processSampleSummary(businessSurveySample, samplingUnitList, expectedCI, collectionExerciseId);

    return sampleSummary;
  }

  /**
   * validate the csv line and return the optional concatenated list of fields
   * failing validation
   *
   * @param csvLine the line
   * @return the errored column names separated by '_'
   */
  private Optional<String> validateLine(SocialSampleUnit csvLine) {
    Set<ConstraintViolation<SocialSampleUnit>> violations = getValidator().validate(csvLine);
    String invalidColumns = violations.stream().map(v -> v.getPropertyPath().toString())
        .collect(Collectors.joining("_"));
    return (invalidColumns.length() == 0) ? Optional.empty() : Optional.ofNullable(invalidColumns);
  }

}
