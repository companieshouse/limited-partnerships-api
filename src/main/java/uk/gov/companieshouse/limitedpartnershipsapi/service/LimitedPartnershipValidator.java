package uk.gov.companieshouse.limitedpartnershipsapi.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class LimitedPartnershipValidator {

    @Autowired
    private Validator validator;


    public List<ValidationStatusError> validate(LimitedPartnershipDto limitedPartnershipDto) {
        Set<ConstraintViolation<LimitedPartnershipDto>> violations = validator.validate(limitedPartnershipDto);

        List<ValidationStatusError> errorsList = new ArrayList<>();
        violations.forEach(v ->
                errorsList.add(createValidationStatusError(v.getMessage(), v.getPropertyPath().toString())));

        // TODO These checks are valid for Partnership Types PFLP and SPFLP (the LP7D CHIPS transaction). Code will
        //      need changing when other Partnership Types need to be validated and sent to CHIPS

        final var dataDto = limitedPartnershipDto.getData();
        if (PartnershipType.PFLP.equals(dataDto.getPartnershipType())
                || PartnershipType.SPFLP.equals(dataDto.getPartnershipType())) {
            if (dataDto.getEmail() == null) {
                errorsList.add(createValidationStatusError("Email is required", "data.email"));
            }

            if (dataDto.getJurisdiction() == null) {
                errorsList.add(createValidationStatusError("Jurisdiction is required", "data.jurisdiction"));
            }

            if (dataDto.getRegisteredOfficeAddress() == null) {
                errorsList.add(createValidationStatusError("Registered office address is required",
                        "data.registeredOfficeAddress"));
            }

            if (dataDto.getPrincipalPlaceOfBusinessAddress() == null) {
                errorsList.add(createValidationStatusError("Principal place of business address is required",
                        "data.principalPlaceOfBusinessAddress"));
            }

            if (dataDto.getTerm() != null) {
                errorsList.add(createValidationStatusError("Term is not required", "data.term"));
            }
        }

        return errorsList;
    }

    private ValidationStatusError createValidationStatusError(String errorMessage, String location) {
        var error = new ValidationStatusError();
        error.setError(errorMessage);
        error.setLocation(location);
        return error;
    }
}
