package uk.gov.ons.ctp.response.sample.mapper;

import java.util.ArrayList;
import java.util.List;
import libs.sample.validation.BusinessSampleUnit;
import uk.gov.ons.ctp.response.sample.domain.model.SampleSummary;
import uk.gov.ons.ctp.response.sample.domain.model.SampleUnit;
import uk.gov.ons.ctp.response.sample.representation.BusinessSampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

public class CustomObjectMapper {

  CustomObjectMapper() {}

  public static SampleSummaryDTO mapSampleSummaryDTO(SampleSummary sampleSummary) {
    SampleSummaryDTO sampleSummaryDTO = new SampleSummaryDTO();
    sampleSummaryDTO.setId(sampleSummary.getId());
    sampleSummaryDTO.setIngestDateTime(sampleSummary.getIngestDateTime());
    sampleSummaryDTO.setState(sampleSummary.getState());
    sampleSummaryDTO.setTotalSampleUnits(sampleSummary.getTotalSampleUnits());
    sampleSummaryDTO.setExpectedCollectionInstruments(
        sampleSummary.getExpectedCollectionInstruments());
    sampleSummaryDTO.setNotes(sampleSummary.getNotes());

    return sampleSummaryDTO;
  }

  public static List<SampleSummaryDTO> mapSampleSummariesToListOfDTO(
      List<SampleSummary> sampleSummaryList) {
    List<SampleSummaryDTO> sampleSummaryDTOList = new ArrayList<>();
    for (SampleSummary sampleSummary : sampleSummaryList) {
      SampleSummaryDTO sampleSummaryDTO = mapSampleSummaryDTO(sampleSummary);
      sampleSummaryDTOList.add(sampleSummaryDTO);
    }

    return sampleSummaryDTOList;
  }

  public static SampleUnitDTO mapSampleUnitDTO(SampleUnit sampleUnit) {
    SampleUnitDTO sampleUnitDTO = new SampleUnitDTO();

    sampleUnitDTO.setSampleUnitPK(sampleUnit.getSampleUnitPK());
    sampleUnitDTO.setSampleSummaryFK(sampleUnit.getSampleSummaryFK());
    sampleUnitDTO.setSampleUnitRef(sampleUnit.getSampleUnitRef());
    sampleUnitDTO.setSampleUnitType(sampleUnit.getSampleUnitType());
    sampleUnitDTO.setFormType(sampleUnit.getFormType());
    sampleUnitDTO.setState(sampleUnit.getState());
    sampleUnitDTO.setPartyId(sampleUnit.getPartyId());
    sampleUnitDTO.setActiveEnrolment(sampleUnit.isActiveEnrolment());
    sampleUnitDTO.setCollectionInstrumentId(sampleUnit.getCollectionInstrumentId());
    sampleUnitDTO.setId(String.valueOf(sampleUnit.getId()));

    return sampleUnitDTO;
  }

  public static List<SampleUnitDTO> mapSampleUnitsToListOfDTO(List<SampleUnit> sampleUnitList) {
    List<SampleUnitDTO> sampleSummaryDTOList = new ArrayList<>();
    for (SampleUnit sampleUnit : sampleUnitList) {
      SampleUnitDTO sampleUnitDTO = mapSampleUnitDTO(sampleUnit);
      sampleSummaryDTOList.add(sampleUnitDTO);
    }

    return sampleSummaryDTOList;
  }

  public static BusinessSampleUnit mapBusinessUnit(BusinessSampleUnitDTO businessSampleUnitDTO) {
    BusinessSampleUnit businessSampleUnit = new BusinessSampleUnit();

    businessSampleUnit.setCheckletter(businessSampleUnitDTO.getCheckletter());

    businessSampleUnit.setFrosic92(businessSampleUnitDTO.getFrosic92());
    businessSampleUnit.setRusic92(businessSampleUnitDTO.getRusic92());
    businessSampleUnit.setFrosic2007(businessSampleUnitDTO.getFrosic2007());
    businessSampleUnit.setRusic2007(businessSampleUnitDTO.getRusic2007());

    businessSampleUnit.setFroempment(businessSampleUnitDTO.getFroempment());
    businessSampleUnit.setFrotover(businessSampleUnitDTO.getFrotover());
    businessSampleUnit.setEntref(businessSampleUnitDTO.getEntref());

    businessSampleUnit.setLegalstatus(businessSampleUnitDTO.getLegalstatus());
    businessSampleUnit.setEntrepmkr(businessSampleUnitDTO.getEntrepmkr());
    businessSampleUnit.setRegion(businessSampleUnitDTO.getRegion());
    businessSampleUnit.setBirthdate(businessSampleUnitDTO.getBirthdate());

    businessSampleUnit.setEntname1(businessSampleUnit.getEntname1());
    businessSampleUnit.setEntname2(businessSampleUnitDTO.getEntname2());
    businessSampleUnit.setEntname3(businessSampleUnitDTO.getEntname3());

    businessSampleUnit.setRuname1(businessSampleUnitDTO.getRuname1());
    businessSampleUnit.setRuname2(businessSampleUnitDTO.getRuname2());
    businessSampleUnit.setRuname3(businessSampleUnitDTO.getRuname3());

    businessSampleUnit.setTradstyle1(businessSampleUnitDTO.getTradstyle1());
    businessSampleUnit.setTradstyle2(businessSampleUnitDTO.getTradstyle2());
    businessSampleUnit.setTradstyle3(businessSampleUnitDTO.getTradstyle3());

    businessSampleUnit.setSeltype(businessSampleUnitDTO.getSeltype());
    businessSampleUnit.setInclexcl(businessSampleUnitDTO.getInclexcl());
    businessSampleUnit.setCell_no(businessSampleUnitDTO.getCellNo());
    businessSampleUnit.setCurrency(businessSampleUnitDTO.getCurrency());

    businessSampleUnit.setSampleUnitRef(businessSampleUnitDTO.getSampleUnitRef());
    businessSampleUnit.setSampleUnitType(businessSampleUnitDTO.getSampleUnitType());
    businessSampleUnit.setFormType(businessSampleUnitDTO.getFormType());

    return businessSampleUnit;
  }
}
