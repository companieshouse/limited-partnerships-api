package uk.gov.companieshouse.limitedpartnershipsapi.validator.personwithsignificantcontrol;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.enums.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.validator.personwithsignificantcontrol.natureofcontrol.NatureOfControlValidator;

import java.util.List;

@Component
public class RelevantLegalEntityValidatorStrategy extends PersonWithSignificantControlValidatorStrategy {

    private final Validator validator;
    private final ValidationStatus validationStatus;

    @Autowired
    public RelevantLegalEntityValidatorStrategy(Validator validator,
                                                ValidationStatus validationStatus,
                                                NatureOfControlValidator natureOfControlValidator) {
        super(natureOfControlValidator);
        this.validator = validator;
        this.validationStatus = validationStatus;
    }

    @Override
    public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
        BindingResult bindingResult = new BeanPropertyBindingResult(personWithSignificantControlDto, DATA_DTO_CLASS_NAME);

        super.validatePartialRleOrOrp(personWithSignificantControlDto, PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY, validator, bindingResult);

        var data = personWithSignificantControlDto.getData();
        validateRegisterDetails(data, bindingResult);

        throwIfErrors(bindingResult);
    }

    private void validateRegisterDetails(PersonWithSignificantControlDataDto data, BindingResult bindingResult) {
        var enteredOnRegister = data.getEnteredOnRegister();
        var registeredCompanyNumber = data.getRegisteredCompanyNumber();
        var legalEntityRegisterName = data.getLegalEntityRegisterName();
        var legalEntityRegistrationLocation = data.getLegalEntityRegistrationLocation();

        var enteredOnRegisterFieldName = "data.enteredOnRegister";
        var registeredCompanyNumberFieldName = "data.registeredCompanyNumber";
        var legalEntityRegisterNameFieldName = "data.legalEntityRegisterName";
        var legalEntityRegistrationLocationFieldName = "data.legalEntityRegistrationLocation";

        if (enteredOnRegister == null) {
            addError(enteredOnRegisterFieldName, "Entered on register is required", bindingResult);
        } else if (enteredOnRegister) {
            checkNotNullOrEmpty(legalEntityRegistrationLocation, legalEntityRegistrationLocationFieldName, "Legal Entity Registration Location is required when entered on register is true", bindingResult);
            checkNotNullOrEmpty(legalEntityRegisterName, legalEntityRegisterNameFieldName, "Legal Entity Register Name is required when entered on register is true", bindingResult);
            checkNotNullOrEmpty(registeredCompanyNumber, registeredCompanyNumberFieldName, "Registered Company Number is required when entered on register is true", bindingResult);
        }
    }

    @Override
    public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto) throws ServiceException {
       return super.validateFullRleOrOrp(personWithSignificantControlDto, validationStatus);
    }
}
