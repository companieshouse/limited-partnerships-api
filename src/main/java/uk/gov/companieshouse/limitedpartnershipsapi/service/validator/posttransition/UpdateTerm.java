package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

@Component
public class UpdateTerm implements PostTransitionStrategy<LimitedPartnershipDto> {
    @Override
    public String getKind() {
        return PartnershipKind.UPDATE_PARTNERSHIP_TERM.getDescription();
    }

    @Override
    public void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
        if (limitedPartnershipDto.getData().getTerm() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Term is required",
                    "data.term"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
