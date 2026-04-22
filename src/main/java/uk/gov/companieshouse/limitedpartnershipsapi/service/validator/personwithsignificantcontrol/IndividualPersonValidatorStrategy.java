package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.ArrayList;
import java.util.List;

@Component
public class IndividualPersonValidatorStrategy extends PersonWithSignificantControlValidatorStrategy {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    @Autowired
    public IndividualPersonValidatorStrategy(Validator validator,
                                                ValidationStatus validationStatus) {
        this.validator = validator;
        this.validationStatus = validationStatus;
    }

    @Override
    public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(personWithSignificantControlDto, DATA_DTO_CLASS_NAME);

        performAnnotationValidation(personWithSignificantControlDto, validator, bindingResult);

        // null checks for mandatory fields
        var data = personWithSignificantControlDto.getData();
        checkNotNullOrEmpty(data.getForename(), "data.forename", "Forename is required", bindingResult);
        checkNotNullOrEmpty(data.getSurname(), "data.surname", "Surname is required", bindingResult);
        if (data.getDateOfBirth() == null) {
            addError("data.dateOfBirth", "Date of birth is required", bindingResult);
        }
        checkNotNullOrEmpty(data.getNationality1(), "data.nationality1", "Nationality 1 is required", bindingResult);

        if (bindingResult.hasErrors()) {
            var methodParameter = new MethodParameter(PersonWithSignificantControlDataDto.class.getConstructor(), -1);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
    }

    @Override
    public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();
        getPartialValidationErrors(personWithSignificantControlDto, validationStatus, errorsList);

        var dataDto = personWithSignificantControlDto.getData();

        if (dataDto.getUsualResidentialAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Usual residential address is required", "data.usualResidentialAddress"));
        }

        if (dataDto.getServiceAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Service address is required", "data.serviceAddress"));
        }

        return errorsList;
    }
}
