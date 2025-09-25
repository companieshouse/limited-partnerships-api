package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

@Component
public class UpdatePrincipalPlaceOfBusinessAddress implements PostTransitionStrategy {

    @Override
    public String getKind() {
        return PartnershipKind.UPDATE_PARTNERSHIP_PRINCIPAL_PLACE_OF_BUSINESS_ADDRESS.getDescription();
    }

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (limitedPartnershipDto.getData().getPrincipalPlaceOfBusinessAddress() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Principal place of business address is required",
                    "data.principalPlaceOfBusinessAddress"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
