package uk.gov.ons.ctp.response.sample.ingest;

import lombok.Data;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

@Data
public class IngesterCTPException extends CTPException {

    private SampleSummaryDTO.ErrorCode errorCode;

    public IngesterCTPException(Fault afault, SampleSummaryDTO.ErrorCode errorCode, String message, Object... args) {
        super(afault, message, args);

        this.errorCode = errorCode;
    }
}
