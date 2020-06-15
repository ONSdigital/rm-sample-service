package libs.sample.validation;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Getter
public class BusinessSampleUnit {

  private static final String NON_BLANK_INTEGER_RE = "[+-]?[\\d]+|^$";
  private static final String DATE_REGEX =
      "^\\s*$|^(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)$";

  @Size(min = 0, max = 1)
  String checkletter;

  @Size(min = 0, max = 5)
  String frosic92;

  @Size(min = 0, max = 5)
  String rusic92;

  @Size(min = 0, max = 5)
  String frosic2007;

  @Size(min = 0, max = 5)
  String rusic2007;

  @Pattern(regexp = NON_BLANK_INTEGER_RE)
  String froempment;

  @Pattern(regexp = NON_BLANK_INTEGER_RE)
  String frotover;

  @Size(min = 0, max = 10)
  String entref;

  @Size(min = 0, max = 1)
  String legalstatus;

  @Size(min = 0, max = 1)
  String entrepmkr;

  @Size(min = 0, max = 2)
  String region;

  @Pattern(regexp = DATE_REGEX)
  String birthdate;

  @Size(min = 0, max = 35)
  String entname1;

  @Size(min = 0, max = 35)
  String entname2;

  @Size(min = 0, max = 35)
  String entname3;

  @NotNull
  @Size(min = 1, max = 35)
  String runame1;

  @Size(min = 0, max = 35)
  String runame2;

  @Size(min = 0, max = 35)
  String runame3;

  @Size(min = 0, max = 35)
  String tradstyle1;

  @Size(min = 0, max = 35)
  String tradstyle2;

  @Size(min = 0, max = 35)
  String tradstyle3;

  @Size(min = 0, max = 1)
  String seltype;

  @Size(min = 0, max = 1)
  String inclexcl;

  @Pattern(regexp = NON_BLANK_INTEGER_RE)
  // CHECKSTYLE IGNORE MemberName FOR NEXT 1 LINES
  String cell_no;

  @Size(min = 0, max = 1)
  String currency;

  String sampleUnitRef;

  String sampleUnitType;

  String formType;

  UUID sampleUnitId = UUID.randomUUID();
}
