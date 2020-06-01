package uk.gov.ons.ctp.response.sample.ingest;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import libs.common.error.CTPException;
import libs.sample.validation.BusinessSampleUnit;
import libs.sample.validation.SampleUnitBase;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;

@Service
public class CsvIngesterBusiness extends CsvToBean<BusinessSampleUnit> {

  private static final Logger log = LoggerFactory.getLogger(CsvIngesterBusiness.class);

  private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(200);

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

  private static final String[] COLUMNS =
      new String[] {
        SAMPLEUNITREF,
        CHECKLETTER,
        FROSIC92,
        RUSIC92,
        FROSIC2007,
        RUSIC2007,
        FROEMPMENT,
        FROTOVER,
        ENTREF,
        LEGALSTATUS,
        ENTREPMKR,
        REGION,
        BIRTHDATE,
        ENTNAME1,
        ENTNAME2,
        ENTNAME3,
        RUNAME1,
        RUNAME2,
        RUNAME3,
        TRADSTYLE1,
        TRADSTYLE2,
        TRADSTYLE3,
        SELTYPE,
        INCLEXCL,
        CELLNO,
        FORMTYPE,
        CURRENCY
      };

  @Autowired private SampleService sampleService;

  @Autowired private PartyPublisher partyPublisher;

  @Autowired private Validator csvIngestValidator;

  private ColumnPositionMappingStrategy<BusinessSampleUnit> columnPositionMappingStrategy;

  public CsvIngesterBusiness() {
    columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
    columnPositionMappingStrategy.setType(BusinessSampleUnit.class);
    columnPositionMappingStrategy.setColumnMapping(COLUMNS);
  }

  public SampleSummary ingest(final SampleSummary sampleSummary, final MultipartFile file)
      throws Exception {

    CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()), ':');
    String[] nextLine;
    List<Callable<BusinessSampleUnit>> callables = new LinkedList<>();
    Set<String> unitRefs = ConcurrentHashMap.newKeySet();
    int currentLineNumber = 1;

    while ((nextLine = csvReader.readNext()) != null) {
      final String[] finalNextLine = nextLine;
      final int finalLineNumber = currentLineNumber++;

      callables.add(
          () -> {
            try {
              return parseLine(finalNextLine, unitRefs);
            } catch (CTPException e) {
              String newMessage = String.format("Line %d: %s", finalLineNumber, e.getMessage());
              throw new CTPException(e.getFault(), e, newMessage);
            }
          });
    }

    List<Future<BusinessSampleUnit>> futures = EXECUTOR_SERVICE.invokeAll(callables);

    List<BusinessSampleUnit> sampleUnitList = new LinkedList<>();
    for (Future<BusinessSampleUnit> future : futures) {
      try {
        sampleUnitList.add(future.get());
      } catch (ExecutionException executionException) {
        if (executionException.getCause().getClass().equals(CTPException.class)) {
          throw (CTPException) executionException.getCause();
        }
      }
    }

    SampleSummary sampleSummaryWithCICount =
        sampleService.saveSample(sampleSummary, sampleUnitList, SampleUnitState.INIT);
    publishToPartyQueue(sampleUnitList, sampleSummary.getId().toString());

    return sampleSummaryWithCICount;
  }

  private BusinessSampleUnit parseLine(String[] nextLine, Set<String> unitRefs)
      throws IllegalAccessException, java.lang.reflect.InvocationTargetException,
          InstantiationException, java.beans.IntrospectionException, CTPException,
          CsvRequiredFieldEmptyException, CsvConstraintViolationException,
          CsvDataTypeMismatchException {
    BusinessSampleUnit businessSampleUnit = null;

    // Liquibase OpenCSV parser is not thread-safe. It's also slow. We should use something better.
    synchronized (columnPositionMappingStrategy) {
      businessSampleUnit = processLine(columnPositionMappingStrategy, nextLine);
    }

    List<String> namesOfInvalidColumns = validateLine(businessSampleUnit);

    synchronized (unitRefs) {
      // If a unit ref is already registered
      if (unitRefs.contains(businessSampleUnit.getSampleUnitRef())) {
        log.warn(
            "sample unit ref is duplicated in the file.",
            kv("sample_unit_ref", businessSampleUnit.getSampleUnitRef()));
        throw new CTPException(
            CTPException.Fault.VALIDATION_FAILED,
            String.format(
                "This sample unit ref %s is duplicated in the file.",
                businessSampleUnit.getSampleUnitRef()));
      }
      unitRefs.add(businessSampleUnit.getSampleUnitRef());
    }

    if (!namesOfInvalidColumns.isEmpty()) {
      String errorMessage =
          String.format(
              "Error in %s due to field(s) %s",
              Arrays.toString(nextLine), Arrays.toString(namesOfInvalidColumns.toArray()));
      log.warn(errorMessage);
      throw new CTPException(CTPException.Fault.VALIDATION_FAILED, errorMessage);
    }
    businessSampleUnit.setSampleUnitType("B");

    return businessSampleUnit;
  }

  private List<String> validateLine(BusinessSampleUnit csvLine) {
    Set<ConstraintViolation<BusinessSampleUnit>> violations = csvIngestValidator.validate(csvLine);
    List<String> invalidFields =
        violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toList());
    if (StringUtils.isEmpty(csvLine.getSampleUnitRef())) {
      invalidFields.add(SAMPLEUNITREF);
    }
    if (StringUtils.isEmpty(csvLine.getFormType())) {
      invalidFields.add(FORMTYPE);
    }
    return invalidFields;
  }

  private void publishToPartyQueue(
      List<? extends SampleUnitBase> samplingUnitList, String sampleSummaryId) {
    for (SampleUnitBase sampleUnitBase : samplingUnitList) {
      PartyCreationRequestDTO party = PartyUtil.convertToParty(sampleUnitBase);
      party.getAttributes().setSampleUnitId(sampleUnitBase.getSampleUnitId().toString());
      party.setSampleSummaryId(sampleSummaryId);
      partyPublisher.publish(party);
    }
  }
}
