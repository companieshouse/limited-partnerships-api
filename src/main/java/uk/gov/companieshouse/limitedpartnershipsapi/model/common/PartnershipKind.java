package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.UPDATE_PARTNERSHIP_NAME_DESCRIPTION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS_DESCRIPTION;

public enum PartnershipKind {
    UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS(UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS_DESCRIPTION),
    UPDATE_PARTNERSHIP_NAME(UPDATE_PARTNERSHIP_NAME_DESCRIPTION),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    PartnershipKind(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
