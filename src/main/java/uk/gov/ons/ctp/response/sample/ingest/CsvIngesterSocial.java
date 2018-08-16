package uk.gov.ons.ctp.response.sample.ingest;

import com.google.common.collect.Sets;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import liquibase.util.csv.opencsv.bean.CsvToBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.domain.model.SampleAttributes;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleAttributesRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;
import uk.gov.ons.ctp.response.sample.service.SampleService;
import validation.SocialSampleUnit;

@Slf4j
@Service
public class CsvIngesterSocial extends CsvToBean<SocialSampleUnit> {

  @Autowired private SampleService sampleService;

  @Autowired private SampleAttributesRepository sampleAttributesRepository;

  @Transactional(propagation = Propagation.REQUIRED)
  public SampleSummary ingest(final SampleSummary sampleSummary, final MultipartFile file)
      throws Exception {

    List<SocialSampleUnit> socialSamples = new ArrayList<>();
    List<SampleAttributes> sampleAttributes = new ArrayList<>();

    final Reader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()));

    try (CSVParser parser =
        CSVParser.parse(
            reader, CSVFormat.RFC4180.withFirstRecordAsHeader().withIgnoreSurroundingSpaces())) {
      Set<String> headers = parser.getHeaderMap().keySet();
      validateHeaders(headers);

      for (CSVRecord line : parser) {
        SocialSampleUnit socialSampleUnit = parseLine(line);
        socialSamples.add(socialSampleUnit);

        sampleAttributes.add(
            new SampleAttributes(
                socialSampleUnit.getSampleUnitId(), socialSampleUnit.getAttributes()));
      }
    }

    sampleService.saveSample(sampleSummary, socialSamples, SampleUnitState.PERSISTED);
    sampleAttributesRepository.save(sampleAttributes);
    sampleService.activateSampleSummaryState(sampleSummary.getSampleSummaryPK());

    return sampleSummary;
  }

  private SocialSampleUnit parseLine(CSVRecord line) throws CTPException {
    SocialSampleUnit sampleUnit = new SocialSampleUnit();
    sampleUnit.setAttributes(line.toMap());
    String ref = line.toMap().get("REFERENCE");
    sampleUnit.setSampleUnitRef(ref);
    List<String> invalidColumns = sampleUnit.validate();
    if (!invalidColumns.isEmpty()) {
      String errorMessage =
          String.format(
              "Error in row [%s] due to missing field(s) [%s]",
              StringUtils.join(line.toMap().values(), ","), StringUtils.join(invalidColumns, ","));
      log.warn(errorMessage);
      throw new CTPException(CTPException.Fault.VALIDATION_FAILED, errorMessage);
    }
    return sampleUnit;
  }

  private void validateHeaders(Set<String> headers) throws CTPException {
    Set<String> missingRequiredHeaders =
        Sets.difference(SocialSampleUnit.REQUIRED_ATTRIBUTES, headers);
    if (!missingRequiredHeaders.isEmpty()) {
      String errorMessage =
          String.format(
              "Error in header row, missing required header(s) [%s]",
              StringUtils.join(missingRequiredHeaders, ","));
      log.warn(errorMessage);
      throw new CTPException(CTPException.Fault.VALIDATION_FAILED, errorMessage);
    }
  }
}
