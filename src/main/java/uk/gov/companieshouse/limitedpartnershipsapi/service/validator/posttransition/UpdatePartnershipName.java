package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

@Component
public class UpdatePartnershipName implements PostTransitionStrategy {

    @Value("${UPDATE_PARTNERSHIP_NAME_COST}")
    private String updatePartnershipNameCost;

    private static final String COST_DESCRIPTION = "Update of Limited Partnership name fee";

    @Override
    public String getKind() {
        return PartnershipKind.UPDATE_PARTNERSHIP_NAME.getDescription();
    }

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (limitedPartnershipDto.getData().getNameEnding() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Name ending is required",
                    "data.nameEnding"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        String productType = "lp-" + getKind().split("#")[1];

        cost.setAmount(updatePartnershipNameCost);
        cost.setDescription(COST_DESCRIPTION);
        cost.setProductType(productType);

        return cost;
    }
}
