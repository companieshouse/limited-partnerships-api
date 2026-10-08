package uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;
import uk.gov.companieshouse.limitedpartnershipsapi.personwithsignificantcontrol.validator.natureofcontrol.NatureOfControlValidator;

import java.util.List;

// Protected Individual Person is not required to be validated, so this strategy is a no-op implementation of the PersonWithSignificantControlValidatorStrategy interface.
@Component
public class ProtectedIndividualPersonValidatorStrategy extends PersonWithSignificantControlValidatorStrategy {

	@Autowired
	ProtectedIndividualPersonValidatorStrategy(NatureOfControlValidator natureOfControlValidator) {
		super(natureOfControlValidator);
	}

	@Override
	public void validatePartial(PersonWithSignificantControlDto personWithSignificantControlDto) throws NoSuchMethodException, MethodArgumentNotValidException, ServiceException {
		// No validation required for Protected Individual Person, so this method is intentionally left empty.
	}

	@Override
	public List<ValidationStatusError> validateFull(PersonWithSignificantControlDto personWithSignificantControlDto) throws ServiceException {
		return List.of();
	}
}
