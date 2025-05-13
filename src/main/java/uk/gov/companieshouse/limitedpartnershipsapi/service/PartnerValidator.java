package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;

import java.util.List;

public abstract class PartnerValidator {

    protected Validator validator;

    @Autowired
    protected PartnerValidator(Validator validator) {
        this.validator = validator;
    }

    protected void convertFieldErrorsToValidationStatusErrors(BindingResult bindingResult, List<ValidationStatusError> errorsList) {
        bindingResult.getFieldErrors().forEach(fe ->
                errorsList.add(createValidationStatusError(fe.getDefaultMessage(), fe.getField())));
    }

    protected ValidationStatusError createValidationStatusError(String errorMessage, String location) {
        var error = new ValidationStatusError();
        error.setError(errorMessage);
        error.setLocation(location);
        return error;
    }
}
