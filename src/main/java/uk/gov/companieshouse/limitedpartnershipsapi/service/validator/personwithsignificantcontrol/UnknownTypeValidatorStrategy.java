package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnknownTypeValidatorStrategy extends PersonWithSignificantControlValidatorStrategy {

    private static final String ERROR_MESSAGE = "Invalid person with significant control type specified";
    private static final String TYPE_FIELD_NAME = "type";

    private final ValidationStatus validationStatus;

    @Autowired
    public UnknownTypeValidatorStrategy(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Override
    public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();
        errorsList.add(validationStatus.createValidationStatusError(ERROR_MESSAGE, TYPE_FIELD_NAME));
        return errorsList;
    }

    @Override
    public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(personWithSignificantControlDto, DATA_DTO_CLASS_NAME);
        addError(TYPE_FIELD_NAME, ERROR_MESSAGE, bindingResult);
        var methodParameter = new MethodParameter(PersonWithSignificantControlDataDto.class.getConstructor(), -1);
        throw new MethodArgumentNotValidException(methodParameter, bindingResult);
    }
}
