package uk.gov.companieshouse.limitedpartnershipsapi.exception;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * This environment variable enables the length of the log output to be truncated. Useful to prevent flooding of the
     * logs by a malicious user, whose text input value may end up being present in an API error message or stack trace.
     */
    @Value("${GLOBAL_EXCEPTION_HANDLER_TRUNCATE_LENGTH_CHARS:15000}")
    private int truncationLength;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest webRequest) {
        var context = webRequest.getHeader(ERIC_REQUEST_ID_KEY);
        var sanitisedExceptionMessage = truncate(Encode.forJava(ex.getMessage()));
        var sanitisedStackTrace = truncate(Encode.forJava(ExceptionUtils.getStackTrace(ex)));
        var sanitisedRootCause = truncate(Encode.forJava(ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(ex))));

        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put("error", ex.getClass());
        logMap.put("stackTrace", sanitisedStackTrace);
        logMap.put("rootCause", sanitisedRootCause);

        ApiLogger.errorContext(context, sanitisedExceptionMessage, null, logMap);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String truncate(String input) {
        return StringUtils.truncate(input, truncationLength);
    }
}
