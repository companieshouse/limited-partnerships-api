package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

public class UpdatePartnershipName implements PostTransitionStrategy {

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (limitedPartnershipDto.getData().getPartnershipName() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Name ending is required",
                    "data.nameEnding"));
        }
    }
}
