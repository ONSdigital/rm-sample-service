package libs.sample.validation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Getter
public class CensusSampleUnit extends SampleUnitBase {

  private static final String POSTCODE_REGEX =
      "^\\s*$|GIR[ ]?0AA|((AB|AL|B|BA|BB|BD|BH|BL|BN|BR|BS|BT|BX|CA|CB|CF|CH|CM|CO|CR|CT|CV|CW|DA|"
          + "DD|DE|DG|DH|DL|DN|DT|DY|E|EC|EH|EN|EX|FK|FY|G|GL|GY|GU|HA|HD|HG|HP|HR|HS|HU|HX|IG|IM|"
          + "IP|IV|JE|KA|KT|KW|KY|L|LA|LD|LE|LL|LN|LS|LU|M|ME|MK|ML|N|NE|NG|NN|NP|NR|NW|OL|OX|PA|"
          + "PE|PH|PL|PO|PR|RG|RH|RM|S|SA|SE|SG|SK|SL|SM|SN|SO|SP|SR|SS|ST|SW|SY|TA|TD|TF|TN|TQ|"
          + "TR|TS|TW|UB|W|WA|WC|WD|WF|WN|WR|WS|WV|YO|ZE)(\\d[\\dA-Z]?[ ]?\\d[ABD-HJLN-UW-Z]{2}))|"
          + "BFPO[ ]?\\d{1,4}";
  private static final String EMAIL_REGEX =
      "^\\s*$|[_A-Za-z0-9\\-\\+]+(\\.[_A-Za-z0-9\\-]+)*@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9]+)*"
          + "(\\.[A-Za-z]{2,})";
  private static final String PHONENUMBER_REGEX =
      "^$^\\s*$|^(?:(?:\\(?(?:0(?:0|11)\\)?[\\s-]?\\(?|\\+)44\\)?[\\s-]?(?:\\(?0\\)?[\\s-]?)?)|"
          + "(?:\\(?0))(?:(?:\\d{5}\\)?[\\s-]?\\d{4,5})|(?:\\d{4}\\)?[\\s-]?(?:\\d{5}|\\d{3}[\\s-]"
          + "?\\d{3}))|(?:\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{3,4})|(?:\\d{2}\\)?[\\s-]?\\d{4}[\\s-]"
          + "?\\d{4}))(?:[\\s-]?(?:x|ext\\.?|\\#)\\d{3,4})?$";

  @NotNull
  @Size(min = 1, max = 60)
  String line1;

  @Size(min = 0, max = 60)
  String line2;

  @Size(min = 0, max = 60)
  String line3;

  @Size(min = 0, max = 60)
  String line4;

  @Size(min = 0, max = 60)
  String line5;

  @Pattern(regexp = POSTCODE_REGEX)
  String postcode;

  @Size(min = 0, max = 20)
  String title;

  @Size(min = 0, max = 35)
  String forename;

  @Size(min = 0, max = 35)
  String surname;

  @Pattern(regexp = PHONENUMBER_REGEX)
  String phoneNumber;

  @Pattern(regexp = EMAIL_REGEX)
  String emailAddress;

  @Size(min = 0, max = 6)
  String addressType;

  @Size(min = 0, max = 6)
  String estabType;

  @Size(min = 0, max = 60)
  String organisationName;

  @Size(min = 0, max = 20)
  String category;

  @NotNull String ladCode;

  @NotNull Double latitude;

  @NotNull Double longitude;

  @NotNull Integer htc;

  String locality;

  @NotNull
  @Size(min = 1, max = 9)
  String oa;

  @NotNull
  @Size(min = 1, max = 9)
  String msoa;

  @NotNull
  @Size(min = 1, max = 9)
  String lsoa;

  @NotNull
  @Size(min = 1, max = 9)
  String censusRegion;
}
