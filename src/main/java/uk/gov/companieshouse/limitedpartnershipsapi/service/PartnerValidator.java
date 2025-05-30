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
        var fieldError = new FieldError(className, fieldName, defaultMessage);
        bindingResult.addError(fieldError);
    }

    protected void checkNotNullLegalEntity(String className, PartnerDataDto partnerDataDto,
                                           BindingResult bindingResult) {

        if (partnerDataDto.getLegalEntityName() == null) {
            addError(className, PartnerDataDto.LEGAL_ENTITY_NAME_FIELD, "Legal Entity Name is required", bindingResult);
        }

        if (partnerDataDto.getLegalForm() == null) {
            addError(className, PartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", bindingResult);
        }

        if (partnerDataDto.getGoverningLaw() == null) {
            addError(className, PartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", bindingResult);
        }

        if (partnerDataDto.getLegalEntityRegisterName() == null) {
            addError(className, PartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required", bindingResult);
        }

        if (partnerDataDto.getLegalEntityRegistrationLocation() == null) {
            addError(className, PartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", bindingResult);
        }

        if (partnerDataDto.getRegisteredCompanyNumber() == null) {
            addError(className, PartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required", bindingResult);
        }
    }

    protected void checkNotNullPerson(String className, PartnerDataDto partnerDataDto,
                                      BindingResult bindingResult) {
        if (partnerDataDto.getForename() == null) {
            addError(className, PartnerDataDto.FORENAME_FIELD, "Forename is required", bindingResult);
        }

        if (partnerDataDto.getSurname() == null) {
            addError(className, PartnerDataDto.SURNAME_FIELD, "Surname is required", bindingResult);
        }

        if (partnerDataDto.getDateOfBirth() == null) {
            addError(className, PartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", bindingResult);
        }

        if (partnerDataDto.getNationality1() == null) {
            addError(className, PartnerDataDto.NATIONALITY1_FIELD, "Nationality1 is required", bindingResult);
        }
    }

}
