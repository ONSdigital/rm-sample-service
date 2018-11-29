package uk.gov.ons.ctp.response.sample.ingest;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import liquibase.util.csv.opencsv.CSVReader;
import liquibase.util.csv.opencsv.bean.ColumnPositionMappingStrategy;
import liquibase.util.csv.opencsv.bean.CsvToBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.CensusSampleUnit;

@Service
public class CsvIngesterCensus extends CsvToBean<CensusSampleUnit> {
  private static final Logger log = LoggerFactory.getLogger(CsvIngesterCensus.class);

  private static final String SAMPLEUNITREF = "sampleUnitRef";
  private static final String FORMTYPE = "formType";
  private static final String LINE1 = "line1";
  private static final String LINE2 = "line2";
  private static final String LINE3 = "line3";
  private static final String LINE4 = "line4";
  private static final String LINE5 = "line5";
  private static final String POSTCODE = "postcode";
  private static final String TITLE = "title";
  private static final String FORENAME = "forename";
  private static final String SURNAME = "surname";
  private static final String PHONENUMBER = "phonenumber";
  private static final String EMAILADDRESS = "emailaddress";
  private static final String ADDRESSTYPE = "addressType";
  private static final String ESTABTYPE = "estabType";
  private static final String ORGANISATIONNAME = "organisationName";
  private static final String CATEGORY = "category";
  private static final String LADCODE = "ladCode";
  private static final String LATITUDE = "latitude";
  private static final String LONGITUDE = "longitude";
  private static final String HTC = "htc";
  private static final String LOCALITY = "locality";
  private static final String OA = "oa";
  private static final String MSOA = "msoa";
  private static final String LSOA = "lsoa";
  private static final String CENSUSREGION = "censusRegion";

  private static final String[] COLUMNS =
      new String[] {
        SAMPLEUNITREF,
        FORMTYPE,
        LINE1,
        LINE2,
        LINE3,
        LINE4,
        LINE5,
        POSTCODE,
        TITLE,
        FORENAME,
        SURNAME,
        PHONENUMBER,
        EMAILADDRESS,
        ADDRESSTYPE,
        ESTABTYPE,
        ORGANISATIONNAME,
        CATEGORY,
        LADCODE,
        LATITUDE,
        LONGITUDE,
        HTC,
        LOCALITY,
        OA,
        MSOA,
        LSOA,
        CENSUSREGION
      };

  @Autowired private SampleService sampleService;
  @Autowired private Validator csvIngestValidator;

  private ColumnPositionMappingStrategy<CensusSampleUnit> columnPositionMappingStrategy;

  public CsvIngesterCensus() {
    columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
    columnPositionMappingStrategy.setType(CensusSampleUnit.class);
    columnPositionMappingStrategy.setColumnMapping(COLUMNS);
  }

  public SampleSummary ingest(final SampleSummary sampleSummary, final MultipartFile file)
      throws Exception {

    CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()), ':');
    String[] nextLine;
    List<CensusSampleUnit> samplingUnitList = new ArrayList<>();
    Integer expectedCI =
        1; // TODO: Census should only have one collection instrument if this is not the case when
    // census is onboarded expectedCI should be calculated

    while ((nextLine = csvReader.readNext()) != null) {
      CensusSampleUnit censusSampleUnit = null;

      // Liquibase OpenCSV parser is not thread-safe. It's also slow. We should use something
      // better.
      synchronized (columnPositionMappingStrategy) {
        censusSampleUnit = processLine(columnPositionMappingStrategy, nextLine);
      }

      Optional<String> namesOfInvalidColumns = validateLine(censusSampleUnit);
      if (namesOfInvalidColumns.isPresent()) {
        log.with("line", Arrays.toString(nextLine))
            .with("invalid_columns", namesOfInvalidColumns.get())
            .error("Problem parsing line, entire ingest aborted");
        throw new CTPException(
            CTPException.Fault.VALIDATION_FAILED,
            String.format(
                "Problem parsing line %s due to %s",
                Arrays.toString(nextLine), namesOfInvalidColumns.get()));
      }

      censusSampleUnit.setSampleUnitType("H");

      samplingUnitList.add(censusSampleUnit);
    }

    return sampleService.saveSample(sampleSummary, samplingUnitList, SampleUnitState.INIT);
  }

  /**
   * validate the csv line and return the optional concatenated list of fields failing validation
   *
   * @param csvLine the line
   * @return the errored column names separated by '_'
   */
  private Optional<String> validateLine(CensusSampleUnit csvLine) {
    Set<ConstraintViolation<CensusSampleUnit>> violations = csvIngestValidator.validate(csvLine);

    String invalidColumns =
        violations
            .stream()
            .map(v -> v.getPropertyPath().toString())
            .collect(Collectors.joining("_"));
    return (invalidColumns.length() == 0) ? Optional.empty() : Optional.ofNullable(invalidColumns);
  }
}
