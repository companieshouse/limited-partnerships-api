package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

@Component
public class UpdatePartnershipName implements PostTransitionStrategy {

    @Value("${LP_REGISTRATION_COST}")
    private String UPDATE_PARTNERSHIP_NAME_COST;

    private static final String REGISTER_COST_DESCRIPTION = "Update of Limited Partnership name";

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        System.out.println("The cost of LP Registration is: " + UPDATE_PARTNERSHIP_NAME_COST);

        if (limitedPartnershipDto.getData().getPartnershipName() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Name ending is required",
                    "data.nameEnding"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        cost.setAmount(UPDATE_PARTNERSHIP_NAME_COST);
        cost.setDescription(REGISTER_COST_DESCRIPTION);

        return cost;
    }
}
