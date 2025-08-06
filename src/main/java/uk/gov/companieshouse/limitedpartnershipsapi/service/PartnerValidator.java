package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public abstract class PartnerValidator {

    protected Validator validator;
    protected CompanyService companyService;

    @Autowired
    protected PartnerValidator(Validator validator, CompanyService companyService) {
        this.validator = validator;
        this.companyService = companyService;
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
        checkFieldNotNull(className, partnerDataDto.getLegalEntityName(), PartnerDataDto.LEGAL_ENTITY_NAME_FIELD, "Legal Entity Name is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getLegalForm(), PartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getGoverningLaw(), PartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getLegalEntityRegisterName(), PartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getLegalEntityRegistrationLocation(), PartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getRegisteredCompanyNumber(), PartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required", bindingResult);
    }

    protected void checkNotNullPerson(String className,
                                      PartnerDataDto partnerDataDto,
                                      BindingResult bindingResult) {
        checkFieldNotNull(className, partnerDataDto.getForename(), PartnerDataDto.FORENAME_FIELD, "Forename is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getSurname(), PartnerDataDto.SURNAME_FIELD, "Surname is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getDateOfBirth(), PartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", bindingResult);
        checkFieldNotNull(className, partnerDataDto.getNationality1(), PartnerDataDto.NATIONALITY1_FIELD, "Nationality1 is required", bindingResult);
    }

    private void checkFieldNotNull(String className, Object value, String fieldName, String errorMessage, BindingResult bindingResult) {
        if (value == null) {
            addError(className, fieldName, errorMessage, bindingResult);
        }
    }

    protected void dtoValidation(String className, PartnerDto partnerDto, BindingResult bindingResult) {
        Set<ConstraintViolation<PartnerDto>> violations = validator.validate(
                partnerDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    addError(className, violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    protected void isSecondNationalityDifferent(String className, PartnerDataDto partnerDataDto, BindingResult bindingResult) {
        String nationality1 = partnerDataDto.getNationality1();
        String nationality2 = partnerDataDto.getNationality2();

        if (nationality1 != null && nationality1.equals(nationality2)) {
            addError(className, PartnerDataDto.NATIONALITY2_FIELD, "Second nationality must be different from the first", bindingResult);
        }
    }

    protected void checkNotNullDateEffectiveFrom(String className, PartnerDto partnerDto, Transaction transaction, BindingResult bindingResult) throws ServiceException {
        if (!transaction.getFilingMode().equals(IncorporationKind.REGISTRATION.getDescription())) {
            if (partnerDto.getData().getDateEffectiveFrom() == null) {
                addError(className, "data.dateEffectiveFrom", "Partner date effective from is required", bindingResult);
            }

            validateDateEffectiveFrom(className, transaction, partnerDto, bindingResult);
        }
    }

    protected void validateDateEffectiveFrom(String className, Transaction transaction, PartnerDto partnerDto, BindingResult bindingResult) throws ServiceException {
        if (partnerDto.getData().getDateEffectiveFrom() != null) {
            CompanyProfileApi companyProfileApi = companyService.getCompanyProfile(transaction.getCompanyNumber());

            LocalDate dateEffectiveFrom = partnerDto.getData().getDateEffectiveFrom();

            if (dateEffectiveFrom.isBefore(companyProfileApi.getDateOfCreation())) {
                addError(className, "data.dateEffectiveFrom", "Partner date effective from cannot be before the incorporation date", bindingResult);
            }
        }
    }
}
