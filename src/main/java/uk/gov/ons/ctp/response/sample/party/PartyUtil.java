package uk.gov.ons.ctp.response.sample.party;

import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestAttributesDTO;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import validation.BusinessSampleUnit;
import validation.SampleUnitBase;

/**
 * Util for the Party Service
 */
public class PartyUtil {

  /**
   * Util method to convert from any of the SampleUnitBase subtypes to the
   * Generic PartyService Party type. All fields other than identity fields will
   * be collected into a key:value map in Party.attributes.
   *
   * @param unit the SampleUnitBase subtype for a Census, Business or Social
   *          SampleUnit
   * @return the created Party object
   * @throws Exception unlikely, but indicated something really wrong
   */
  public static PartyCreationRequestDTO convertToParty(SampleUnitBase unit) throws Exception {
    PartyCreationRequestDTO party = new PartyCreationRequestDTO();
    party.setSampleUnitType(unit.getSampleUnitType());
    party.setSampleUnitRef(unit.getSampleUnitRef());
    if (unit instanceof BusinessSampleUnit) {
      PartyCreationRequestAttributesDTO businessSampleUnit = new PartyCreationRequestAttributesDTO();
      BusinessSampleUnit bsu = (BusinessSampleUnit) unit;
      businessSampleUnit.setCheckletter(bsu.getCheckletter());
      businessSampleUnit.setFrosic92(bsu.getFrosic92());
      businessSampleUnit.setRusic92(bsu.getRusic92());
      businessSampleUnit.setFrosic2007(bsu.getFrosic2007());
      businessSampleUnit.setRusic2007(bsu.getRusic2007());
      businessSampleUnit.setFroempment(Integer.valueOf(bsu.getFroempment()));
      businessSampleUnit.setFrotover(Integer.valueOf(bsu.getFrotover()));
      businessSampleUnit.setEntref(bsu.getEntref());
      businessSampleUnit.setLegalstatus(bsu.getLegalstatus());
      businessSampleUnit.setEntrepmkr(bsu.getEntrepmkr());
      businessSampleUnit.setRegion(bsu.getRegion());
      businessSampleUnit.setBirthdate(bsu.getBirthdate());
      businessSampleUnit.setEntname1(bsu.getEntname1());
      businessSampleUnit.setEntname2(bsu.getEntname2());
      businessSampleUnit.setEntname3(bsu.getEntname3());
      businessSampleUnit.setRuname1(bsu.getRuname1());
      businessSampleUnit.setRuname2(bsu.getRuname2());
      businessSampleUnit.setRuname3(bsu.getRuname3());
      businessSampleUnit.setTradstyle1(bsu.getTradstyle1());
      businessSampleUnit.setTradstyle2(bsu.getTradstyle2());
      businessSampleUnit.setTradstyle3(bsu.getTradstyle3());
      businessSampleUnit.setSeltype(bsu.getSeltype());
      businessSampleUnit.setInclexcl(bsu.getInclexcl());
      businessSampleUnit.setFrotover(Integer.valueOf(bsu.getCell_no()));
      businessSampleUnit.setFormtype(bsu.getFormType());
      businessSampleUnit.setCurrency(bsu.getCurrency());
      party.setAttributes(businessSampleUnit);
    }
    return party;
  }

}
