package uk.gov.ons.ctp.response.sample.party;

import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestAttributesDTO;
import uk.gov.ons.ctp.response.party.definition.PartyCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.definition.SampleUnitBase;

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
    if (unit instanceof uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit) {
      PartyCreationRequestAttributesDTO businessSampleUnit = new PartyCreationRequestAttributesDTO();
      uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit bsu = (uk.gov.ons.ctp.response.sample.definition.BusinessSampleUnit) unit;
      businessSampleUnit.setCheckletter(bsu.getCheckletter());
      businessSampleUnit.setFrosic92(bsu.getFrosic92());
      businessSampleUnit.setRusic92(bsu.getRusic92());
      businessSampleUnit.setFrosic2007(bsu.getFrosic2007());
      businessSampleUnit.setRusic2007(bsu.getRusic2007());
      businessSampleUnit.setFroempment(bsu.getFroempment());
      businessSampleUnit.setFrotover(bsu.getFrotover());
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
      businessSampleUnit.setCellNo(bsu.getCellNo());
      businessSampleUnit.setFormtype(bsu.getFormtype());
      businessSampleUnit.setCurrency(bsu.getCurrency());
      party.setAttributes(businessSampleUnit);
    }
    return party;
  }

  /**
   * Utility to go from Party to PartyCreationRequestDTO
   *
   * @param party the party object
   * @return the PartyCreationRequestDTO
   */
/*  public static PartyCreationRequestDTO createPartyCreationRequestDTO(Party party) {
    PartyCreationRequestDTO partyCreationRequestDTO = new PartyCreationRequestDTO();
    partyCreationRequestDTO.setSampleUnitRef(party.getSampleUnitRef());
    partyCreationRequestDTO.setSampleUnitType(party.getSampleUnitType());
    PartyCreationRequestAttributesDTO attributes = new PartyCreationRequestAttributesDTO();
    attributes.setCheckletter(party.getAttributes().getCheckletter());
    attributes.setFrosic92(party.getAttributes().getFrosic92());
    attributes.setRusic92(party.getAttributes().getRusic92());
    attributes.setFrosic2007(party.getAttributes().getFrosic2007());
    attributes.setRusic2007(party.getAttributes().getRusic2007());
    attributes.setFroempment(party.getAttributes().getFroempment());
    attributes.setFrotover(party.getAttributes().getFrotover());
    attributes.setEntref(party.getAttributes().getEntref());
    attributes.setLegalstatus(party.getAttributes().getLegalstatus());
    attributes.setEntrepmkr(party.getAttributes().getEntrepmkr());
    attributes.setRegion(party.getAttributes().getRegion());
    attributes.setBirthdate(party.getAttributes().getBirthdate());
    attributes.setEntname1(party.getAttributes().getEntname1());
    attributes.setEntname2(party.getAttributes().getEntname2());
    attributes.setEntname3(party.getAttributes().getEntname3());
    attributes.setRuname1(party.getAttributes().getRuname1());
    attributes.setRuname2(party.getAttributes().getRuname2());
    attributes.setRuname3(party.getAttributes().getRuname3());
    attributes.setTradstyle1(party.getAttributes().getTradstyle1());
    attributes.setTradstyle2(party.getAttributes().getTradstyle2());
    attributes.setTradstyle3(party.getAttributes().getTradstyle3());
    attributes.setSeltype(party.getAttributes().getSeltype());
    attributes.setInclexcl(party.getAttributes().getInclexcl());
    attributes.setCellNo(party.getAttributes().getCellNo());
    attributes.setFormtype(party.getAttributes().getFormtype());
    attributes.setCurrency(party.getAttributes().getCurrency());
    partyCreationRequestDTO.setAttributes(attributes);

    return partyCreationRequestDTO;
  }*/
}
