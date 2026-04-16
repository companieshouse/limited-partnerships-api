package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.personwithsignificantcontrol;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;

import java.util.List;

@Component
public class IndividualPersonValidator extends PersonWithSignificantControlValidator {


    @Override
    public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws ServiceException {
        return List.of();
    }

    @Override
    public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {

    }

    @Override
    public void validateUpdate(PersonWithSignificantControlDto personWithSignificantControlDto, Transaction transaction) throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        validatePartial(personWithSignificantControlDto, transaction);
    }
}
