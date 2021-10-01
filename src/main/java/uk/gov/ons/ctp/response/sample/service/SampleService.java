package uk.gov.ons.ctp.response.sample.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import libs.common.error.CTPException;
import libs.common.state.StateTransitionManager;
import libs.common.time.DateTimeUtil;
import libs.sample.validation.BusinessSampleUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleSummaryRepository;
import uk.gov.ons.ctp.response.sample.domain.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO.SampleState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitEvent;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitState;

@Service
@Configuration
public class SampleService {
  private static final Logger log = LoggerFactory.getLogger(SampleService.class);

  @Autowired private SampleSummaryRepository sampleSummaryRepository;

  @Autowired private SampleUnitRepository sampleUnitRepository;

  @Autowired
  @Qualifier("sampleSummaryTransitionManager")
  private StateTransitionManager<SampleState, SampleEvent> sampleSvcStateTransitionManager;

  @Autowired
  @Qualifier("sampleUnitTransitionManager")
  private StateTransitionManager<SampleUnitState, SampleUnitEvent>
      sampleSvcUnitStateTransitionManager;

  public List<SampleSummary> findAllSampleSummaries() {
    return sampleSummaryRepository.findAll();
  }

  public SampleSummary findSampleSummary(UUID id) {
    return sampleSummaryRepository.findById(id).orElse(null);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public SampleSummary saveSample(
      SampleSummary sampleSummary,
      List<BusinessSampleUnit> samplingUnitList,
      SampleUnitState sampleUnitState) {
    int expectedCI = calculateExpectedCollectionInstruments(samplingUnitList);

    sampleSummary.setTotalSampleUnits(samplingUnitList.size());
    sampleSummary.setExpectedCollectionInstruments(expectedCI);
    SampleSummary savedSampleSummary = sampleSummaryRepository.save(sampleSummary);
    saveSampleUnits(samplingUnitList, savedSampleSummary, sampleUnitState);

    return savedSampleSummary;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public SampleUnit createSampleUnit(
      UUID sampleSummaryId, BusinessSampleUnit samplingUnit, SampleUnitState sampleUnitState)
      throws UnknownSampleSummaryException, CTPException {

    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId).orElse(null);
    if (sampleSummary != null) {
      // the csv ingester does this so it's needed here
      samplingUnit.setSampleUnitType("B");

      /** a sample unit should be unique inside a sample summary, so check if we already have it. */
      boolean exists =
          sampleUnitRepository.existsBySampleUnitRefAndSampleSummaryFK(
              samplingUnit.getSampleUnitRef(), sampleSummary.getSampleSummaryPK());
      if (!exists) {
        SampleUnit sampleUnit =
            createAndSaveSampleUnit(sampleSummary, sampleUnitState, samplingUnit);
        updateState(sampleUnit);
        return sampleUnit;
      } else {
        throw new IllegalStateException("sample unit already exists");
      }
    } else {
      throw new UnknownSampleSummaryException();
    }
  }

  private Integer calculateExpectedCollectionInstruments(
      List<BusinessSampleUnit> samplingUnitList) {
    // TODO: get survey classifiers from survey service, currently using formtype for all business
    // surveys
    Set<String> formTypes = new HashSet<>();
    for (BusinessSampleUnit businessSampleUnit : samplingUnitList) {
      formTypes.add(businessSampleUnit.getFormType());
    }
    return formTypes.size();
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public SampleSummary createAndSaveSampleSummary() {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setState(SampleState.ACTIVE);
    sampleSummary.setId(UUID.randomUUID());

    return sampleSummaryRepository.save(sampleSummary);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public SampleSummary createAndSaveSampleSummary(SampleSummaryDTO summaryDTO) {
    SampleSummary sampleSummary = new SampleSummary();
    sampleSummary.setState(SampleState.ACTIVE);
    sampleSummary.setId(UUID.randomUUID());
    sampleSummary.setTotalSampleUnits(summaryDTO.getTotalSampleUnits());
    sampleSummary.setExpectedCollectionInstruments(summaryDTO.getExpectedCollectionInstruments());
    sampleSummary.setIngestDateTime(DateTimeUtil.nowUTC());
    log.debug("about to save sample summary");
    return sampleSummaryRepository.save(sampleSummary);
  }

  private void saveSampleUnits(
      List<BusinessSampleUnit> samplingUnitList,
      SampleSummary sampleSummary,
      SampleUnitState sampleUnitState) {
    for (BusinessSampleUnit sampleUnitBase : samplingUnitList) {
      createAndSaveSampleUnit(sampleSummary, sampleUnitState, sampleUnitBase);
    }
  }

  private SampleUnit createAndSaveSampleUnit(
      SampleSummary sampleSummary,
      SampleUnitState sampleUnitState,
      BusinessSampleUnit sampleUnitBase) {
    SampleUnit sampleUnit = new SampleUnit();
    sampleUnit.setSampleSummaryFK(sampleSummary.getSampleSummaryPK());
    sampleUnit.setSampleUnitRef(sampleUnitBase.getSampleUnitRef());
    sampleUnit.setSampleUnitType(sampleUnitBase.getSampleUnitType());
    sampleUnit.setFormType(sampleUnitBase.getFormType());
    sampleUnit.setState(sampleUnitState);
    sampleUnit.setId(sampleUnitBase.getSampleUnitId());
    sampleUnitRepository.save(sampleUnit);
    return sampleUnit;
  }

  public void updateState(SampleUnit sampleUnit) throws CTPException {
    changeSampleUnitState(sampleUnit);
  }

  private void changeSampleUnitState(SampleUnit sampleUnit) throws CTPException {
    SampleUnitState newState =
        sampleSvcUnitStateTransitionManager.transition(
            sampleUnit.getState(), SampleUnitEvent.PERSISTING);
    sampleUnit.setState(newState);
    sampleUnitRepository.saveAndFlush(sampleUnit);
  }

  public int getSampleSummaryUnitCount(UUID sampleSummaryId) {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId).orElse(null);
    if (sampleSummary == null) {
      throw new IllegalArgumentException(
          String.format("Sample summary %s cannot be found", sampleSummaryId));
    } else if (sampleSummary.getTotalSampleUnits() == null) {
      throw new IllegalStateException(
          String.format("Sample summary %s has no total sample units set", sampleSummaryId));
    }

    return sampleSummary.getTotalSampleUnits().intValue();
  }

  // TODO get this to get the attributes in a separate call then stitch the results into the value
  // returned by sampleUnitRepository.findById
  public SampleUnit findSampleUnit(UUID id) {
    return sampleUnitRepository.findById(id).orElse(null);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public List<SampleUnit> findSampleUnitsBySampleSummaryAsList(UUID sampleSummaryId) {
    return this.findSampleUnitsBySampleSummary(sampleSummaryId).collect(Collectors.toList());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Stream<SampleUnit> findSampleUnitsBySampleSummary(UUID sampleSummaryId) {
    try {
      SampleSummary ss = sampleSummaryRepository.findById(sampleSummaryId).orElseThrow();
      return sampleUnitRepository.findBySampleSummaryFK(ss.getSampleSummaryPK());
    } catch (NoSuchElementException e) {
      log.error("unable to find sample summary", kv("sampleSummaryId", sampleSummaryId));
      return Collections.EMPTY_LIST.stream();
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public List<SampleUnit> findSampleUnitsBySampleSummaryAndStateAsList(
      UUID sampleSummaryId, SampleUnitState state) {
    return this.findSampleUnitsBySampleSummaryAndState(sampleSummaryId, state)
        .collect(Collectors.toList());
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Stream<SampleUnit> findSampleUnitsBySampleSummaryAndState(
      UUID sampleSummaryId, SampleUnitState state) {
    try {
      SampleSummary ss =
          sampleSummaryRepository
              .findById(sampleSummaryId)
              .orElseThrow(UnknownSampleSummaryException::new);
      return sampleUnitRepository.findBySampleSummaryFKAndState(ss.getSampleSummaryPK(), state);
    } catch (UnknownSampleSummaryException e) {
      log.error("unable to find sample summary", kv("sampleSummaryId", sampleSummaryId));
      return Collections.EMPTY_LIST.stream();
    }
  }

  public SampleUnit findSampleUnitBySampleSummaryAndSampleUnitRef(
      UUID sampleSummaryId, String sampleUnitRef)
      throws UnknownSampleSummaryException, UnknownSampleUnitException {
    SampleSummary sampleSummary = sampleSummaryRepository.findById(sampleSummaryId).orElse(null);
    if (sampleSummary != null) {
      /** a sample unit should be unique inside a sample summary, so check if we already have it. */
      SampleUnit sampleUnit =
          sampleUnitRepository.findBySampleUnitRefAndSampleSummaryFK(
              sampleUnitRef, sampleSummary.getSampleSummaryPK());
      if (sampleUnit != null) {
        return sampleUnit;
      } else {
        throw new UnknownSampleUnitException();
      }
    } else {
      throw new UnknownSampleSummaryException();
    }
  }
}
