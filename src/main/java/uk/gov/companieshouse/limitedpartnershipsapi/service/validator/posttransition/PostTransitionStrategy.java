package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

public interface PostTransitionStrategy {
    void validate(LimitedPartnershipDto limitedPartnershipDto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus);
}
