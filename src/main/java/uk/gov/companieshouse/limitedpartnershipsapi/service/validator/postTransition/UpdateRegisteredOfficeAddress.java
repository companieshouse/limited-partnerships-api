package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.postTransition;

import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

public class UpdateRegisteredOfficeAddress implements PostTransitionStrategy {

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (limitedPartnershipDto.getData().getRegisteredOfficeAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Registered office address is required",
                    "data.registeredOfficeAddress"));
        }
    }
}
