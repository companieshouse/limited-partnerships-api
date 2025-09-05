package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS_DESCRIPTION;

@Component
public class UpdateRegisteredOfficeAddress implements PostTransitionStrategy {

    @Override
    public String getKind() {
        return UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS_DESCRIPTION;
    }

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (limitedPartnershipDto.getData().getRegisteredOfficeAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Registered office address is required",
                    "data.registeredOfficeAddress"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
