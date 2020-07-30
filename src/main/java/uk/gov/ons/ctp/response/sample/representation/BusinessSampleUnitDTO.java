package uk.gov.ons.ctp.response.sample.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
public class BusinessSampleUnitDTO {
  private String checkLetter;

  private String frosic92;

  private String rusic92;

  private String frosic2007;

  private String rusic2007;

  private String froempment;

  private String frotover;

  private String entref;

  private String legalStatus;

  private String entrepmkr;

  private String region;

  private String birthdate;

  private String entname1;

  private String entname2;

  private String entname3;

  private String runame1;

  private String runame2;

  private String runame3;

  private String tradstyle1;

  private String tradstyle2;

  private String tradstyle3;

  private String seltype;

  private String inclexcl;

  private String cellNo;

  private String currency;

  private String sampleUnitRef;

  private String sampleUnitType;

  private String formType;
}
