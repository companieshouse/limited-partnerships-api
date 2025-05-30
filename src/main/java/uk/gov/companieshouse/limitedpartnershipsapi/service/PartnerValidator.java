package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;

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

    protected void addError(String className, String fieldName, String defaultMessage, BindingResult bindingResult) {
        bindingResult.addError(new FieldError(className, fieldName, defaultMessage));
    }

    protected void checkNotNullLegalEntity(String className,
                                           PartnerDataDto partnerDataDto,
                                           BindingResult bindingResult) {
        checkField(partnerDataDto.getLegalEntityName(), className, PartnerDataDto.LEGAL_ENTITY_NAME_FIELD, "Legal Entity Name is required", bindingResult);
        checkField(partnerDataDto.getLegalForm(), className, PartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", bindingResult);
        checkField(partnerDataDto.getGoverningLaw(), className, PartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", bindingResult);
        checkField(partnerDataDto.getLegalEntityRegisterName(), className, PartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required", bindingResult);
        checkField(partnerDataDto.getLegalEntityRegistrationLocation(), className, PartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", bindingResult);
        checkField(partnerDataDto.getRegisteredCompanyNumber(), className, PartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required", bindingResult);
    }

    protected void checkNotNullPerson(String className,
                                      PartnerDataDto partnerDataDto,
                                      BindingResult bindingResult) {
        checkField(partnerDataDto.getForename(), className, PartnerDataDto.FORENAME_FIELD, "Forename is required", bindingResult);
        checkField(partnerDataDto.getSurname(), className, PartnerDataDto.SURNAME_FIELD, "Surname is required", bindingResult);
        checkField(partnerDataDto.getDateOfBirth(), className, PartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", bindingResult);
        checkField(partnerDataDto.getNationality1(), className, PartnerDataDto.NATIONALITY1_FIELD, "Nationality1 is required", bindingResult);
    }

    private void checkField(Object value, String className, String fieldName, String errorMessage, BindingResult bindingResult) {
        if (value == null) {
            addError(className, fieldName, errorMessage, bindingResult);
        }
    }
}
