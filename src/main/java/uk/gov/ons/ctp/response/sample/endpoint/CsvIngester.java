package uk.gov.ons.ctp.response.sample.endpoint;

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
import validation.BusinessSampleUnitVerify;
import validation.BusinessSampleUnitsVerify;
import validation.BusinessSurveySampleVerify;

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
public class CsvIngester extends CsvToBean<BusinessSampleUnitVerify> {

  private static final String SURVEYREF = "surveyRef";
  private static final String COLLECTIONEXERCISEREF = "collectionExerciseRef";
  private static final String EFFECTIVESTARTDATETIME = "effectiveStartDateTime";
  private static final String EFFECTIVEENDDATETIME = "effectiveEndDateTime";
  private static final String SAMPLEUNITREF = "sampleUnitRef";
  private static final String SAMPLEUNITTYPE = "sampleUnitType";
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
  private static final String FORMTYPE2 = "formtype2";
  private static final String CURRENCY = "currency";

  private static final String[] COLUMNS = new String[] {SURVEYREF, COLLECTIONEXERCISEREF, EFFECTIVESTARTDATETIME, EFFECTIVEENDDATETIME, SAMPLEUNITREF, SAMPLEUNITTYPE, FORMTYPE, CHECKLETTER, FROSIC92, RUSIC92, FROSIC2007, RUSIC2007,
      FROEMPMENT, FROTOVER, ENTREF, LEGALSTATUS, ENTREPMKR, REGION, BIRTHDATE, ENTNAME1, ENTNAME2, ENTNAME3,
      RUNAME1, RUNAME2, RUNAME3, TRADSTYLE1, TRADSTYLE2, TRADSTYLE3, SELTYPE, INCLEXCL, CELLNO, FORMTYPE2,
      CURRENCY};

  @Autowired
  private SampleService sampleService;

  private ColumnPositionMappingStrategy<BusinessSampleUnitVerify> columnPositionMappingStrategy;

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

  public CsvIngester() {
    columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
    columnPositionMappingStrategy.setType(BusinessSampleUnitVerify.class);
    columnPositionMappingStrategy.setColumnMapping(COLUMNS);
  }

  public SampleSummary ingest(MultipartFile file)
      throws Exception {

    CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()));
    String[] nextLine;
    SampleSummary sampleSummary;
    BusinessSurveySampleVerify businessSurveySample = new BusinessSurveySampleVerify(null);
    List<BusinessSampleUnitVerify> samplingUnitList = new ArrayList<>();
    int lineNum = 0;

      while((nextLine = csvReader.readNext()) != null) {

        if (lineNum == 1) {
          businessSurveySample.setSurveyRef(nextLine[0]);
          businessSurveySample.setCollectionExerciseRef(nextLine[1]);
          businessSurveySample.setEffectiveStartDateTime(nextLine[2]);
          businessSurveySample.setEffectiveEndDateTime(nextLine[3]);
        }

        if (lineNum++ > 0) {
          BusinessSampleUnitVerify businessSampleUnitVerify = processLine(columnPositionMappingStrategy, nextLine);
          Optional<String> namesOfInvalidColumns = validateLine(businessSampleUnitVerify);
          if (namesOfInvalidColumns.isPresent()) {
            log.error("Problem parsing line {} due to {} - entire ingest aborted", Arrays.toString(nextLine),
                namesOfInvalidColumns.get());
            throw new CTPException(CTPException.Fault.VALIDATION_FAILED, String.format("Problem parsing line %s due to %s", Arrays.toString(nextLine),
                namesOfInvalidColumns.get()));
          }

          samplingUnitList.add(businessSampleUnitVerify);

        }

      }

      BusinessSampleUnitsVerify businessSampleUnitsVerify = new BusinessSampleUnitsVerify();
      businessSampleUnitsVerify.setBusinessSampleUnits(samplingUnitList);

      businessSurveySample.setSampleUnits(businessSampleUnitsVerify);

      sampleSummary = sampleService.processSampleSummary(businessSurveySample, samplingUnitList);

    return sampleSummary;
  }

  /**
   * validate the csv line and return the optional concatenated list of fields
   * failing validation
   *
   * @param csvLine the line
   * @return the errored column names separated by '_'
   */
  private Optional<String> validateLine(BusinessSampleUnitVerify csvLine) {
    Set<ConstraintViolation<BusinessSampleUnitVerify>> violations = getValidator().validate(csvLine);
    String invalidColumns = violations.stream().map(v -> v.getPropertyPath().toString())
        .collect(Collectors.joining("_"));
    return (invalidColumns.length() == 0) ? Optional.empty() : Optional.ofNullable(invalidColumns);
  }

}
