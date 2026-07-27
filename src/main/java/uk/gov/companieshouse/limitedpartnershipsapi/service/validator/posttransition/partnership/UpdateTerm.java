package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partnership;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class UpdateTerm implements PostTransitionStrategy<PartnershipDto> {
    @Override
    public String getKind() {
        return PartnershipKind.UPDATE_PARTNERSHIP_TERM.getDescription();
    }

    @Override
    public void validate(PartnershipDto partnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus, Transaction transaction) {
        if (partnershipDto.getData().getTerm() == null) {
            errorsList.add(validationStatus.createValidationStatusError("Term is required",
                    "data.term"));
        }
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
