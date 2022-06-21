package uk.gov.ons.ctp.response.sample.message.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SampleDeadLetter {

  private String sampleUnitRef;
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
  private String birthDate;
  private String entName1;
  private String entName2;
  private String entName3;
  private String ruName1;
  private String ruName2;
  private String ruName3;
  private String tradStyle1;
  private String tradStyle2;
  private String tradStyle3;
  private String selType;
  private String inclexcl;
  private String cellNo;
  private String formType;
  private String currency;
  private String sampleSummaryId;
  private String msg;
}
