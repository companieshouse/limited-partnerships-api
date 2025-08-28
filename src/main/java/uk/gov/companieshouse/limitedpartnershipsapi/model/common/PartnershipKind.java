package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PartnershipKind {
    UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS("limited-partnership#update-partnership-registered-office-address"),
    UPDATE_PARTNERSHIP_NAME("limited-partnership#update-partnership-name"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    PartnershipKind(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static PartnerKind fromDescription(String description) {
        for (PartnerKind kind : PartnerKind.values()) {
            if (kind.getDescription().equalsIgnoreCase(description)) {
                return kind;
            }
        }

        return PartnerKind.UNKNOWN;
    }
}
