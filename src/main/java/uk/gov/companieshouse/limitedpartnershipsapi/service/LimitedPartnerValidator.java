package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnerValidator {

    @Autowired
    private Validator validator;


    public List<ValidationStatusError> validate(LimitedPartnerDto limitedPartnerDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        executeJavaBeansValidation(limitedPartnerDto, errorsList);

        var dataDto = limitedPartnerDto.getData();
        if (dataDto.isLegalEntity()) {
            if (dataDto.getPrincipalOfficeAddress() == null) {
                errorsList.add(createValidationStatusError("Principal office address is required", LimitedPartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
            }
        } else {
            if (dataDto.getUsualResidentialAddress() == null) {
                errorsList.add(createValidationStatusError("Usual residential address is required", LimitedPartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD));
            }
        }

        return errorsList;
    }

    public void dataValidator(LimitedPartnerDto limitedPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        var limitedPartnerDataDto = limitedPartnerDto.getData();

        if (limitedPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(limitedPartnerDataDto, bindingResult);

        } else if (limitedPartnerDataDto.getForename() != null || limitedPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(limitedPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(limitedPartnerDto, bindingResult);
        } else {
            addError("", "Some fields are missing", bindingResult);
        }

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void checkNotNullLegalEntity(LimitedPartnerDataDto limitedPartnerDataDto,
                                         BindingResult bindingResult) {

        if (limitedPartnerDataDto.getLegalEntityName() == null) {
            addError(LimitedPartnerDataDto.LEGAL_ENTITY_NAME_FIELD, "Legal Entity Name is required", bindingResult);
        }

        if (limitedPartnerDataDto.getLegalForm() == null) {
            addError(LimitedPartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", bindingResult);
        }

        if (limitedPartnerDataDto.getGoverningLaw() == null) {
            addError(LimitedPartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", bindingResult);
        }

        if (limitedPartnerDataDto.getLegalEntityRegisterName() == null) {
            addError(LimitedPartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required", bindingResult);
        }

        if (limitedPartnerDataDto.getLegalEntityRegistrationLocation() == null) {
            addError(LimitedPartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", bindingResult);
        }

        if (limitedPartnerDataDto.getRegisteredCompanyNumber() == null) {
            addError(LimitedPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required", bindingResult);
        }
    }

    private void checkNotNullPerson(LimitedPartnerDataDto limitedPartnerDataDto,
                                    BindingResult bindingResult) {
        if (limitedPartnerDataDto.getForename() == null) {
            addError(LimitedPartnerDataDto.FORENAME_FIELD, "Forename is required", bindingResult);
        }

        if (limitedPartnerDataDto.getSurname() == null) {
            addError(LimitedPartnerDataDto.SURNAME_FIELD, "Surname is required", bindingResult);
        }

        if (limitedPartnerDataDto.getDateOfBirth() == null) {
            addError(LimitedPartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", bindingResult);
        }

        if (limitedPartnerDataDto.getNationality1() == null) {
            addError(LimitedPartnerDataDto.NATIONALITY1_FIELD, "Nationality1 is required", bindingResult);
        }
    }

    public void isSecondNationalityDifferent(LimitedPartnerDto limitedPartnerDto, BindingResult bindingResult) {
        String nationality1 = limitedPartnerDto.getData().getNationality1();
        String nationality2 = limitedPartnerDto.getData().getNationality2();

        if (nationality1 != null && nationality1.equals(nationality2)) {
            addError(LimitedPartnerDataDto.NATIONALITY2_FIELD, "Second nationality must be different from the first", bindingResult);
        }
    }

    private void addError(String fieldName, String defaultMessage, BindingResult bindingResult) {
        var fieldError = new FieldError(LimitedPartnerDataDto.class.getName(), fieldName, defaultMessage);
        bindingResult.addError(fieldError);
    }

    private void executeJavaBeansValidation(LimitedPartnerDto limitedPartnerDto, List<ValidationStatusError> errorsList)
            throws ServiceException {
        Set<ConstraintViolation<LimitedPartnerDto>> violations = validator.validate(limitedPartnerDto);

        violations.stream().forEach(v ->
                errorsList.add(createValidationStatusError(v.getMessage(), v.getPropertyPath().toString())));

        try {
            dataValidator(limitedPartnerDto);
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private void convertFieldErrorsToValidationStatusErrors(BindingResult bindingResult, List<ValidationStatusError> errorsList) {
        bindingResult.getFieldErrors().stream().forEach(fe ->
                errorsList.add(createValidationStatusError(fe.getDefaultMessage(), fe.getField())));
    }

    private ValidationStatusError createValidationStatusError(String errorMessage, String location) {
        var error = new ValidationStatusError();
        error.setError(errorMessage);
        error.setLocation(location);
        return error;
    }
}

