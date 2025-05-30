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
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class GeneralPartnerValidator extends PartnerValidator {
    private static final String CLASS_NAME = GeneralPartnerDataDto.class.getName();

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
            checkNotNullLegalEntity(CLASS_NAME, generalPartnerDataDto, bindingResult);
            if (Boolean.FALSE.equals(generalPartnerDataDto.getLegalPersonalityStatementChecked())) {
                addError(CLASS_NAME, GeneralPartnerDataDto.LEGAL_PERSONALITY_STATEMENT_CHECKED_FIELD, "Legal Personality Statement must be checked", bindingResult);
            }
        } else if (generalPartnerDataDto.getForename() != null || generalPartnerDataDto.getSurname() != null) {
            checkNotNullPerson(CLASS_NAME, generalPartnerDataDto, bindingResult);
            isSecondNationalityDifferent(generalPartnerDto, bindingResult);
            if (Boolean.FALSE.equals(generalPartnerDataDto.getNotDisqualifiedStatementChecked())) {
                addError(CLASS_NAME, GeneralPartnerDataDto.NOT_DISQUALIFIED_STATEMENT_CHECKED_FIELD, "Not Disqualified Statement must be checked", bindingResult);
            }
        } else {
            addError(CLASS_NAME, "", "Some fields are missing", bindingResult);
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
                    addError(CLASS_NAME, violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    public void isSecondNationalityDifferent(GeneralPartnerDto generalPartnerDto, BindingResult bindingResult) {
        String nationality1 = generalPartnerDto.getData().getNationality1();
        String nationality2 = generalPartnerDto.getData().getNationality2();

        if (nationality1 != null && nationality1.equals(nationality2)) {
            addError(CLASS_NAME, GeneralPartnerDataDto.NATIONALITY2_FIELD, "Second nationality must be different from the first", bindingResult);
        }
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
