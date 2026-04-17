package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import jakarta.validation.Validator;
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
public class RelevantLegalEntityValidatorStrategy extends PersonWithSignificantControlValidatorStrategy {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    @Autowired
    public RelevantLegalEntityValidatorStrategy(Validator validator,
                                                ValidationStatus validationStatus) {
        this.validator = validator;
        this.validationStatus = validationStatus;
    }

    @Override
    public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
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

    @Override
    public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws ServiceException {
        List<ValidationStatusError> errorsList = new ArrayList<>();

        try {
            validatePartial(personWithSignificantControlDto, transaction);
        } catch (MethodArgumentNotValidException e) {
            validationStatus.convertFieldErrorsToValidationStatusErrors(e.getBindingResult(), errorsList);
        } catch (NoSuchMethodException e) {
            throw new ServiceException(e.getMessage());
        }

        var dataDto = personWithSignificantControlDto.getData();

        if (dataDto.getPrincipalOfficeAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Principal office address is required", "data.principalOfficeAddress"));
        }

        return errorsList;
    }
}
