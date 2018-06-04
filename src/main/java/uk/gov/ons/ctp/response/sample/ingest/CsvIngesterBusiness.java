package uk.gov.ons.ctp.response.sample.ingest;

import liquibase.util.csv.opencsv.CSVReader;
import liquibase.util.csv.opencsv.bean.ColumnPositionMappingStrategy;
import liquibase.util.csv.opencsv.bean.CsvToBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnit;
import validation.BusinessSurveySample;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Slf4j
@Service
public class CsvIngesterBusiness extends CsvToBean<BusinessSampleUnit> {

  private static final String SAMPLEUNITREF = "sampleUnitRef";
  private static final String FORMTYPE = "formType";
  private static final String CHECKLETTER = "checkletter";
  private static final String FROSIC92 = "frosic92";
  private static final String RUSIC92 = "rusic92";
  private static final String FROSIC2007 = "frosic2007";
  private static final String RUSIC2007 = "rusic2007";
  private static final String FROEMPMENT = "froempment";
  private static final String FROTOVER = "frotover";
  private static final String ENTREF = "entref";
  private static final String LEGALSTATUS = "legalstatus";
  private static final String ENTREPMKR = "entrepmkr";
  private static final String REGION = "region";
  private static final String BIRTHDATE = "birthdate";
  private static final String ENTNAME1 = "entname1";
  private static final String ENTNAME2 = "entname2";
  private static final String ENTNAME3 = "entname3";
  private static final String RUNAME1 = "runame1";
  private static final String RUNAME2 = "runame2";
  private static final String RUNAME3 = "runame3";
  private static final String TRADSTYLE1 = "tradstyle1";
  private static final String TRADSTYLE2 = "tradstyle2";
  private static final String TRADSTYLE3 = "tradstyle3";
  private static final String SELTYPE = "seltype";
  private static final String INCLEXCL = "inclexcl";
  private static final String CELLNO = "cell_no";
  private static final String CURRENCY = "currency";

  private static final String[] COLUMNS = new String[] {SAMPLEUNITREF, CHECKLETTER, FROSIC92, RUSIC92, FROSIC2007,
      RUSIC2007, FROEMPMENT, FROTOVER, ENTREF, LEGALSTATUS, ENTREPMKR, REGION, BIRTHDATE, ENTNAME1, ENTNAME2, ENTNAME3,
      RUNAME1, RUNAME2, RUNAME3, TRADSTYLE1, TRADSTYLE2, TRADSTYLE3, SELTYPE, INCLEXCL, CELLNO, FORMTYPE, CURRENCY};

  private enum ValidationErrorType {
    InvalidColumns, DuplicateRU
  }

  @Data
  @AllArgsConstructor
  private static class ValidationError {
      private int lineNumber;
      private ValidationErrorType errorType;
      private String errorDetail;
  }

  @Autowired
  private SampleService sampleService;

  @Autowired
  private AppConfig appConfig;

  private ColumnPositionMappingStrategy<BusinessSampleUnit> columnPositionMappingStrategy;

  /**
   * Lazy create a reusable validator
   *
   * @return the cached validator
   */
  @Cacheable(cacheNames = "csvIngestValidator")
  public Validator getValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }

  public CsvIngesterBusiness() {
    columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
    columnPositionMappingStrategy.setType(BusinessSampleUnit.class);
    columnPositionMappingStrategy.setColumnMapping(COLUMNS);
  }

  private String generateErrorReport(int maxErrors, List<BusinessSampleUnit> sampleUnitList, List<ValidationError> errors){
    StringBuffer report = new StringBuffer();

    report.append(String.format("timestamp: %s, valid samples: %d, errors: %d (fail at %d errors)\n",
            new Date().toString(), sampleUnitList.size(), errors.size(), maxErrors));
    report.append(errors
        .stream()
        .map(ve -> String.format("Line %d: %s -> %s", ve.getLineNumber(), ve.getErrorType().name(), ve.getErrorDetail()))
        .collect(Collectors.joining("\n")));

    return report.toString();
  }

  private void validateFilename(String filename) throws IngesterCTPException {
    if (!filename.toLowerCase().endsWith(".csv")){
      throw new IngesterCTPException(CTPException.Fault.VALIDATION_FAILED, SampleSummaryDTO.ErrorCode.NotCsv,
              String.format("%s is not a valid CSV file (must have .csv extension)", filename));
    }
  }

  public SampleSummary ingest(final SampleSummary sampleSummary, final MultipartFile file)
      throws Exception {

      validateFilename(file.getOriginalFilename());

      int maxErrors = this.appConfig.getSampleIngest().getMaxErrors();
      CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()), ':');
      String[] nextLine;
      List<BusinessSampleUnit> samplingUnitList = new ArrayList<>();
      int lineNumber = 0;
      Set<String> unitRefs = new HashSet<>();
      List<ValidationError> errors = new ArrayList<>();

      while((nextLine = csvReader.readNext()) != null && errors.size() < maxErrors) {
        lineNumber++;

        BusinessSampleUnit businessSampleUnit = processLine(columnPositionMappingStrategy, nextLine);
        Optional<String> namesOfInvalidColumns = validateLine(businessSampleUnit);

        if (namesOfInvalidColumns.isPresent() || unitRefs.contains(businessSampleUnit.getSampleUnitRef())) {
          if (namesOfInvalidColumns.isPresent()) {
            log.error("Problem parsing line {} due to {}", Arrays.toString(nextLine), namesOfInvalidColumns.get());
            errors.add(new ValidationError(lineNumber, ValidationErrorType.InvalidColumns, namesOfInvalidColumns.get()));
          }
          if (unitRefs.contains(businessSampleUnit.getSampleUnitRef())) {
            log.error("This sample unit ref {} is duplicated in the file.", businessSampleUnit.getSampleUnitRef());
            errors.add(new ValidationError(lineNumber, ValidationErrorType.DuplicateRU,
                    businessSampleUnit.getSampleUnitRef()));
          }
        } else {
          businessSampleUnit.setSampleUnitType("B");

          samplingUnitList.add(businessSampleUnit);
        }

        unitRefs.add(businessSampleUnit.getSampleUnitRef());
      }

      if (errors.size() > 0){
          String errorReport = generateErrorReport(maxErrors, samplingUnitList, errors);

          throw new IngesterCTPException(CTPException.Fault.VALIDATION_FAILED, SampleSummaryDTO.ErrorCode.DataError, errorReport);
      }

      return sampleService.processSampleSummary(sampleSummary, samplingUnitList);
  }

  /**
   * validate the csv line and return the optional concatenated list of fields
   * failing validation
   *
   * @param csvLine the line
   * @return the errored column names separated by '_'
   */
  private Optional<String> validateLine(BusinessSampleUnit csvLine) {
    Set<ConstraintViolation<BusinessSampleUnit>> violations = getValidator().validate(csvLine);
    String invalidColumns = violations.stream().map(v -> v.getPropertyPath().toString())
        .collect(Collectors.joining("_"));
    return (invalidColumns.length() == 0) ? Optional.empty() : Optional.ofNullable(invalidColumns);
  }

}
