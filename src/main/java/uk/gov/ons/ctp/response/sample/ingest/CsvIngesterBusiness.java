package uk.gov.ons.ctp.response.sample.ingest;

import liquibase.util.StringUtils;
import liquibase.util.csv.opencsv.CSVReader;
import liquibase.util.csv.opencsv.bean.ColumnPositionMappingStrategy;
import liquibase.util.csv.opencsv.bean.CsvToBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.config.AppConfig;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.message.PartyPublisher;
import uk.gov.ons.ctp.response.sample.party.PartyUtil;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.BusinessSampleUnit;
import validation.SampleUnitBase;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final String[] COLUMNS = new String[]{SAMPLEUNITREF, CHECKLETTER, FROSIC92, RUSIC92, FROSIC2007,
            RUSIC2007, FROEMPMENT, FROTOVER, ENTREF, LEGALSTATUS, ENTREPMKR, REGION, BIRTHDATE, ENTNAME1, ENTNAME2, ENTNAME3,
            RUNAME1, RUNAME2, RUNAME3, TRADSTYLE1, TRADSTYLE2, TRADSTYLE3, SELTYPE, INCLEXCL, CELLNO, FORMTYPE, CURRENCY};
    @Autowired
    private SampleService sampleService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private PartyPublisher partyPublisher;

    private ColumnPositionMappingStrategy<BusinessSampleUnit> columnPositionMappingStrategy;

    public CsvIngesterBusiness() {
        columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
        columnPositionMappingStrategy.setType(BusinessSampleUnit.class);
        columnPositionMappingStrategy.setColumnMapping(COLUMNS);
    }

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

    private String generateErrorReport(int maxErrors, List<BusinessSampleUnit> sampleUnitList, List<ValidationError> errors) {
        StringBuffer report = new StringBuffer();

        report.append(String.format("timestamp: %s, valid samples: %d, errors: %d (fail at %d errors)\n",
                new Date().toString(), sampleUnitList.size(), errors.size(), maxErrors));
        report.append(errors
                .stream()
                .map(ve -> String.format("Line %d: %s -> %s", ve.getLineNumber(), ve.getErrorType().name(), ve.getErrorDetail()))
                .collect(Collectors.joining("\n")));

        return report.toString();
    }

    Pair<List<BusinessSampleUnit>,List<ValidationError>> parseAndValidateFile(final MultipartFile file, int maxErrors)
            throws Exception {

        CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()), ':');
        String[] nextLine;
        int lineNumber = 0;
        List<BusinessSampleUnit> sampleUnitList = new ArrayList<>();
        Set<String> unitRefs = new HashSet<>();
        List<ValidationError> errors = new ArrayList<>();

        while ((nextLine = csvReader.readNext()) != null && errors.size() < maxErrors) {
            lineNumber++;
            try {
                Pair<BusinessSampleUnit, List<ValidationError>> pair = parseLine(nextLine, unitRefs, lineNumber);
                List<ValidationError> newErrors = pair.getValue();

                if (newErrors.size() > 0){
                    errors.addAll(newErrors);
                } else {
                    sampleUnitList.add(pair.getKey());
                }
            } catch (CTPException e) {
                String newMessage = String.format("Line %d: %s", csvReader.getRecordsRead(), e.getMessage());
                throw new CTPException(e.getFault(), e, newMessage);
            }
        }

        return new ImmutablePair<>(sampleUnitList, errors);
    }

    public SampleSummary ingest(final SampleSummary sampleSummary, final MultipartFile file)
            throws Exception {

        int maxErrors = this.appConfig.getSampleIngest().getMaxErrors();

        Pair<List<BusinessSampleUnit>, List<ValidationError>> result = parseAndValidateFile(file, maxErrors);
        List<ValidationError> errors = result.getValue();
        List<BusinessSampleUnit> sampleUnitList = result.getKey();

        if (errors.size() > 0) {
            String errorReport = generateErrorReport(maxErrors, sampleUnitList, errors);

            throw new IngesterException(CTPException.Fault.VALIDATION_FAILED, SampleSummaryDTO.ErrorCode.DataError, errorReport);
        }

        SampleSummary sampleSummaryWithCICount = sampleService.saveSample(sampleSummary, sampleUnitList, SampleUnitState.INIT);
        publishToPartyQueue(sampleUnitList, sampleSummary.getId().toString());

        return sampleSummaryWithCICount;
    }

    private Pair<BusinessSampleUnit, List<ValidationError>> parseLine(String[] nextLine, Set<String> unitRefs, int lineNumber) throws IllegalAccessException, java.lang.reflect.InvocationTargetException, InstantiationException, java.beans.IntrospectionException, CTPException {
        BusinessSampleUnit businessSampleUnit = processLine(columnPositionMappingStrategy, nextLine);
        List<String> namesOfInvalidColumns = validateLine(businessSampleUnit);

        List<ValidationError> errors = namesOfInvalidColumns
            .stream()
            .map(columnName -> {
                log.warn( String.format("Error in %s due to field(s) %s", Arrays.toString(nextLine), columnName));
                return new ValidationError(lineNumber, ValidationErrorType.InvalidColumns, columnName);
            })
            .collect(Collectors.toList());

        // If a unit ref is already registered
        if (unitRefs.contains(businessSampleUnit.getSampleUnitRef())) {
            log.warn("This sample unit ref {} is duplicated in the file.", businessSampleUnit.getSampleUnitRef());
            errors.add(new ValidationError(lineNumber, ValidationErrorType.DuplicateRU,
                    businessSampleUnit.getSampleUnitRef()));
        }
        unitRefs.add(businessSampleUnit.getSampleUnitRef());

        if (errors.size() == 0){
            businessSampleUnit.setSampleUnitType("B");

            return new ImmutablePair(businessSampleUnit, errors);
        } else {
            return new ImmutablePair(null, errors);
        }
    }

    private List<String> validateLine(BusinessSampleUnit csvLine) {
        Set<ConstraintViolation<BusinessSampleUnit>> violations = getValidator().validate(csvLine);
        List<String> invalidFields = violations.stream().map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toList());
        if (StringUtils.isEmpty(csvLine.getSampleUnitRef())) {
            invalidFields.add(SAMPLEUNITREF);
        }
        if (StringUtils.isEmpty(csvLine.getFormType())) {
            invalidFields.add(FORMTYPE);
        }
        return invalidFields;
    }

    private void publishToPartyQueue(List<? extends SampleUnitBase> samplingUnitList, String sampleSummaryId) {
        for (SampleUnitBase sampleUnitBase : samplingUnitList) {
            PartyCreationRequestDTO party = PartyUtil.convertToParty(sampleUnitBase);
            party.getAttributes().setSampleUnitId(sampleUnitBase.getSampleUnitId().toString());
            party.setSampleSummaryId(sampleSummaryId);
            partyPublisher.publish(party);
        }
    }

    enum ValidationErrorType {
        InvalidColumns, DuplicateRU
    }

    @Data
    @AllArgsConstructor
    static class ValidationError {
        private int lineNumber;
        private ValidationErrorType errorType;
        private String errorDetail;
    }
}
