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
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class GeneralPartnerValidator extends PartnerValidator {

    @Autowired
    public GeneralPartnerValidator(Validator validator) {
        super(validator);
    }

    public List<ValidationStatusError> validateFull(GeneralPartnerDto generalPartnerDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(generalPartnerDto, errorsList);

        var dataDto = generalPartnerDto.getData();
        if (dataDto.isLegalEntity()) {
            if (dataDto.getPrincipalOfficeAddress() == null) {
                errorsList.add(createValidationStatusError("Principal office address is required", GeneralPartnerDataDto.PRINCIPAL_OFFICE_ADDRESS_FIELD));
            }
        } else {
            if (dataDto.getUsualResidentialAddress() == null) {
                errorsList.add(createValidationStatusError("Usual residential address is required", GeneralPartnerDataDto.USUAL_RESIDENTIAL_ADDRESS_FIELD));
            }

            if (dataDto.getServiceAddress() == null) {
                errorsList.add(createValidationStatusError("Service address is required", GeneralPartnerDataDto.SERVICE_ADDRESS_FIELD));
            }
        }

        return errorsList;
    }

    public void validatePartial(GeneralPartnerDto generalPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(generalPartnerDto, bindingResult);

        var generalPartnerDataDto = generalPartnerDto.getData();

        if (generalPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(generalPartnerDataDto, bindingResult);
            if (Boolean.FALSE.equals(generalPartnerDataDto.getLegalPersonalityStatementChecked())) {
                addError(GeneralPartnerDataDto.LEGAL_PERSONALITY_STATEMENT_CHECKED_FIELD, "Legal Personality Statement must be checked", bindingResult);
            }
        } else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(generalPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(generalPartnerDto, bindingResult);
            if (Boolean.FALSE.equals(generalPartnerDataDto.getNotDisqualifiedStatementChecked())) {
                addError(GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD, "Not Disqualified Statement must be checked", bindingResult);
            }
        } else {
            addError("", "Some fields are missing", bindingResult);
        }

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateUpdate(GeneralPartnerDto generalPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(generalPartnerDto, GeneralPartnerDataDto.class.getName());

        dtoValidation(generalPartnerDto, bindingResult);

        isSecondNationalityDifferent(generalPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(GeneralPartnerDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void dtoValidation(GeneralPartnerDto generalPartnerDto, BindingResult bindingResult) {
        Set<ConstraintViolation<GeneralPartnerDto>> violations = validator.validate(
                generalPartnerDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    addError(violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    private void checkNotNullLegalEntity(GeneralPartnerDataDto generalPartnerDataDto,
                                         BindingResult bindingResult) {

        if (generalPartnerDataDto.getLegalEntityName() == null) {
            addError(GeneralPartnerDataDto.LEGAL_ENTITY_NAME_FIELD, "Legal Entity Name is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalForm() == null) {
            addError(GeneralPartnerDataDto.LEGAL_FORM_FIELD, "Legal Form is required", bindingResult);
        }

        if (generalPartnerDataDto.getGoverningLaw() == null) {
            addError(GeneralPartnerDataDto.GOVERNING_LAW_FIELD, "Governing Law is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalEntityRegisterName() == null) {
            addError(GeneralPartnerDataDto.LEGAL_ENTITY_REGISTER_NAME_FIELD, "Legal Entity Register Name is required", bindingResult);
        }

        if (generalPartnerDataDto.getLegalEntityRegistrationLocation() == null) {
            addError(GeneralPartnerDataDto.LEGAL_ENTITY_REGISTRATION_LOCATION_FIELD, "Legal Entity Registration Location is required", bindingResult);
        }

        if (generalPartnerDataDto.getRegisteredCompanyNumber() == null) {
            addError(GeneralPartnerDataDto.REGISTERED_COMPANY_NUMBER_FIELD, "Registered Company Number is required", bindingResult);
        }
    }

    private void checkNotNullPerson(GeneralPartnerDataDto generalPartnerDataDto,
                                    BindingResult bindingResult) {
        if (generalPartnerDataDto.getForename() == null) {
            addError(GeneralPartnerDataDto.FORENAME_FIELD, "Forename is required", bindingResult);
        }

        if (generalPartnerDataDto.getSurname() == null) {
            addError(GeneralPartnerDataDto.SURNAME_FIELD, "Surname is required", bindingResult);
        }

        if (generalPartnerDataDto.getDateOfBirth() == null) {
            addError(GeneralPartnerDataDto.DATE_OF_BIRTH_FIELD, "Date of birth is required", bindingResult);
        }

        if (generalPartnerDataDto.getNationality1() == null) {
            addError(GeneralPartnerDataDto.NATIONALITY1_FIELD, "Nationality1 is required", bindingResult);
        }
    }

    public void isSecondNationalityDifferent(GeneralPartnerDto generalPartnerDto, BindingResult bindingResult) {
        String nationality1 = generalPartnerDto.getData().getNationality1();
        String nationality2 = generalPartnerDto.getData().getNationality2();

        if (nationality1 != null && nationality1.equals(nationality2)) {
            addError(GeneralPartnerDataDto.NATIONALITY2_FIELD, "Second nationality must be different from the first", bindingResult);
        }
    }

    private void addError(String fieldName, String defaultMessage, BindingResult bindingResult) {
        var fieldError = new FieldError(GeneralPartnerDataDto.class.getName(), fieldName, defaultMessage);
        bindingResult.addError(fieldError);
    }

    private void checkFieldConstraints(GeneralPartnerDto generalPartnerDto, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(generalPartnerDto);
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
