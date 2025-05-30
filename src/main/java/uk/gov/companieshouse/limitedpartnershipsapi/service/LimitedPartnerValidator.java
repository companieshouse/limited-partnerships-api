package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnerValidator extends PartnerValidator {
    private static final String CLASS_NAME = LimitedPartnerDataDto.class.getName();

    @Autowired
    public LimitedPartnerValidator(Validator validator) {
        super(validator);
    }

    public List<ValidationStatusError> validateFull(LimitedPartnerDto limitedPartnerDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        checkFieldConstraints(limitedPartnerDto, errorsList);

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

    public void validatePartial(LimitedPartnerDto limitedPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(limitedPartnerDto, bindingResult);

        var limitedPartnerDataDto = limitedPartnerDto.getData();

        if (limitedPartnerDataDto.isLegalEntity()) {
            checkNotNullLegalEntity(CLASS_NAME, limitedPartnerDataDto, bindingResult);
            if (Boolean.FALSE.equals(limitedPartnerDataDto.getLegalPersonalityStatementChecked())) {
                addError(CLASS_NAME, LimitedPartnerDataDto.LEGAL_PERSONALITY_STATEMENT_CHECKED_FIELD, "Legal Personality Statement must be checked", bindingResult);
            }
        } else if (limitedPartnerDataDto.getForename() != null || limitedPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(CLASS_NAME, limitedPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(limitedPartnerDto, bindingResult);
        } else {
            addError(CLASS_NAME, "", "Some fields are missing", bindingResult);
        }

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    public void validateUpdate(LimitedPartnerDto limitedPartnerDto) throws NoSuchMethodException, MethodArgumentNotValidException {
        var methodParameter = new MethodParameter(LimitedPartnerDataDto.class.getConstructor(), -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(limitedPartnerDto, LimitedPartnerDataDto.class.getName());

        dtoValidation(limitedPartnerDto, bindingResult);

        isSecondNationalityDifferent(limitedPartnerDto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    private void dtoValidation(LimitedPartnerDto limitedPartnerDto, BindingResult bindingResult) {
        Set<ConstraintViolation<LimitedPartnerDto>> violations = validator.validate(
                limitedPartnerDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    addError(CLASS_NAME, violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    public void isSecondNationalityDifferent(LimitedPartnerDto limitedPartnerDto, BindingResult bindingResult) {
        String nationality1 = limitedPartnerDto.getData().getNationality1();
        String nationality2 = limitedPartnerDto.getData().getNationality2();

        if (nationality1 != null && nationality1.equals(nationality2)) {
            addError(CLASS_NAME, LimitedPartnerDataDto.NATIONALITY2_FIELD, "Second nationality must be different from the first", bindingResult);
        }
    }

    private void checkFieldConstraints(LimitedPartnerDto limitedPartnerDto, List<ValidationStatusError> errorsList)
            throws ServiceException {
        try {
            validatePartial(limitedPartnerDto);
        } catch (MethodArgumentNotValidException e) {
            convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}

