package uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition;

import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.util.List;

public interface PostTransitionStrategy<T> {
    String getKind();

    void validate(T dto, List<ValidationStatusError> errorsList, ValidationStatus validationStatus);

    Cost getCost(Cost cost);
}
