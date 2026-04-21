package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;

import java.util.List;
import java.util.Set;

public abstract class PersonWithSignificantControlValidatorStrategy {

    protected static final String DATA_DTO_CLASS_NAME = PersonWithSignificantControlDataDto.class.getName();

    public abstract List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws ServiceException;
    public abstract void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException;

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
}