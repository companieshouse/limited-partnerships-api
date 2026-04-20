package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

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
    public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
       super.validatePartialRleOrOrp(personWithSignificantControlDto, validator);
    }

    @Override
    public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto) throws ServiceException {
       return super.validateFullRleOrOrp(personWithSignificantControlDto, validationStatus);
    }
}
