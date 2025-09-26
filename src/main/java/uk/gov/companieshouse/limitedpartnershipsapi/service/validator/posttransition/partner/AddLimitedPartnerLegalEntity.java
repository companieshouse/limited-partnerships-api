package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.partner;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategy;

import java.util.List;

@Component
public class AddLimitedPartnerLegalEntity implements PostTransitionStrategy<PartnerDto> {
    @Override
    public String getKind() {
        return PartnerKind.ADD_LIMITED_PARTNER_LEGAL_ENTITY.getDescription();
    }

    @Override
    public void validate(PartnerDto partnerDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus) {
    }

    @Override
    public Cost getCost(Cost cost) {
        return null;
    }
}
