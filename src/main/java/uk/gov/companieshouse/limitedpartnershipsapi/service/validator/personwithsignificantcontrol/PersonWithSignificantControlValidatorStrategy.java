package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class PersonWithSignificantControlValidatorStrategy {

    protected static final String DATA_DTO_CLASS_NAME = PersonWithSignificantControlDataDto.class.getName();

    public abstract List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto) throws ServiceException;
    public abstract void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException;

    protected void performAnnotationValidation(PersonWithSignificantControlDto personWithSignificantControlDto, Validator validator, BindingResult bindingResult) {
        Set<ConstraintViolation<PersonWithSignificantControlDto>> violations = validator.validate(
                personWithSignificantControlDto);

        if (!violations.isEmpty()) {
            violations.forEach(violation ->
                    addError(violation.getPropertyPath().toString(), violation.getMessage(), bindingResult)
            );
        }
    }

    protected void addError(String fieldName, String defaultMessage, BindingResult bindingResult) {
        bindingResult.addError(new FieldError(DATA_DTO_CLASS_NAME, fieldName, defaultMessage));
    }

    /**
     * Checks if the given value is null or empty and adds an error to the binding result if so.
     */
    protected void checkNotNullOrEmpty(String value, String fieldName, String errorMessage, BindingResult bindingResult) {
        if (!StringUtils.hasText(value)) {
            addError(fieldName, errorMessage, bindingResult);
        }
    }

    protected void validatePartialRleOrOrp(PersonWithSignificantControlDto personWithSignificantControlDto, Validator validator) throws NoSuchMethodException, MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(personWithSignificantControlDto, DATA_DTO_CLASS_NAME);

        performAnnotationValidation(personWithSignificantControlDto, validator, bindingResult);

        // null checks for mandatory fields
        var data = personWithSignificantControlDto.getData();
        checkNotNullOrEmpty(data.getLegalEntityName(), "data.legalEntityName", "Name is required", bindingResult);
        checkNotNullOrEmpty(data.getLegalForm(), "data.legalForm", "Legal form is required", bindingResult);
        checkNotNullOrEmpty(data.getGoverningLaw(), "data.governingLaw", "Governing law is required", bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(PersonWithSignificantControlDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    protected List<ValidationStatusError> validateFullRleOrOrp(PersonWithSignificantControlDto personWithSignificantControlDto, ValidationStatus validationStatus) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();
        getPartialValidationErrors(personWithSignificantControlDto, validationStatus, errorsList);

        var dataDto = personWithSignificantControlDto.getData();
        if (dataDto.getPrincipalOfficeAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Principal office address is required", "data.principalOfficeAddress"));
        }

        return errorsList;
    }

    protected void getPartialValidationErrors(PersonWithSignificantControlDto personWithSignificantControlDto, ValidationStatus validationStatus, List<ValidationStatusError> errorsList) throws ServiceException {
        try {
            validatePartial(personWithSignificantControlDto);
        } catch (MethodArgumentNotValidException e) {
            validationStatus.convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}