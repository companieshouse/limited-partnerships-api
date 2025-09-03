package uk.gov.companieshouse.limitedpartnershipsapi.service.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;

import java.util.List;

@Component
public class ValidationStatus {

    public ValidationStatusError createValidationStatusError(String errorMessage, String location) {
        var error = new ValidationStatusError();
        error.setError(errorMessage);
        error.setLocation(location);
        return error;
    }

    public void convertFieldErrorsToValidationStatusErrors(BindingResult bindingResult, List<ValidationStatusError> errorsList) {
        bindingResult.getFieldErrors().forEach(fe ->
                errorsList.add(createValidationStatusError(fe.getDefaultMessage(), fe.getField())));
    }
}
