package uk.gov.ons.ctp.response.sample.party;

import libs.sample.validation.BusinessSampleUnit;
import org.apache.commons.lang3.StringUtils;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestAttributesDTO;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;

/** Util for the Party Service */
public class PartyUtil {

  /**
   * Util method to convert from any of the SampleUnitBase subtypes to the Generic PartyService
   * Party type. All fields other than identity fields will be collected into a key:value map in
   * Party.attributes.
   *
   * @param unit the SampleUnitBase subtype for a Census, Business or Social SampleUnit
   * @return the created Party object
   */
  public static PartyCreationRequestDTO convertToParty(BusinessSampleUnit unit) {
    PartyCreationRequestDTO party = new PartyCreationRequestDTO();
    party.setSampleUnitType(unit.getSampleUnitType());
    party.setSampleUnitRef(unit.getSampleUnitRef());
    PartyCreationRequestAttributesDTO businessSampleUnit = new PartyCreationRequestAttributesDTO();
    businessSampleUnit.setCheckletter(unit.getCheckletter());
    businessSampleUnit.setFrosic92(unit.getFrosic92());
    businessSampleUnit.setRusic92(unit.getRusic92());
    businessSampleUnit.setFrosic2007(unit.getFrosic2007());
    businessSampleUnit.setRusic2007(unit.getRusic2007());
    if (StringUtils.isNumeric(unit.getFroempment())) {
      businessSampleUnit.setFroempment(Integer.valueOf(unit.getFroempment()));
    }
    if (StringUtils.isNumeric(unit.getFrotover())) {
      businessSampleUnit.setFrotover(Integer.valueOf(unit.getFrotover()));
    }
    businessSampleUnit.setEntref(unit.getEntref());
    businessSampleUnit.setLegalstatus(unit.getLegalstatus());
    businessSampleUnit.setEntrepmkr(unit.getEntrepmkr());
    businessSampleUnit.setRegion(unit.getRegion());
    businessSampleUnit.setBirthdate(unit.getBirthdate());
    businessSampleUnit.setEntname1(unit.getEntname1());
    businessSampleUnit.setEntname2(unit.getEntname2());
    businessSampleUnit.setEntname3(unit.getEntname3());
    businessSampleUnit.setRuname1(unit.getRuname1());
    businessSampleUnit.setRuname2(unit.getRuname2());
    businessSampleUnit.setRuname3(unit.getRuname3());
    businessSampleUnit.setTradstyle1(unit.getTradstyle1());
    businessSampleUnit.setTradstyle2(unit.getTradstyle2());
    businessSampleUnit.setTradstyle3(unit.getTradstyle3());
    businessSampleUnit.setSeltype(unit.getSeltype());
    businessSampleUnit.setInclexcl(unit.getInclexcl());
    if (StringUtils.isNumeric(unit.getCell_no())) {
      businessSampleUnit.setFrotover(Integer.valueOf(unit.getCell_no()));
    }
    businessSampleUnit.setFormtype(unit.getFormType());
    businessSampleUnit.setCurrency(unit.getCurrency());
    party.setAttributes(businessSampleUnit);

    return party;
  }
}
