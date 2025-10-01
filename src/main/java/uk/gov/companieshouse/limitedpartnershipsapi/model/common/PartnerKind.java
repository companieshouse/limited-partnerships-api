package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PartnerKind {
    ADD_GENERAL_PARTNER_PERSON("limited-partnership#add-general-partner-person"),
    ADD_GENERAL_PARTNER_LEGAL_ENTITY("limited-partnership#add-general-partner-legal-entity"),
    ADD_LIMITED_PARTNER_PERSON("limited-partnership#add-limited-partner-person"),
    ADD_LIMITED_PARTNER_LEGAL_ENTITY("limited-partnership#add-limited-partner-legal-entity"),
    REMOVE_GENERAL_PARTNER_PERSON("limited-partnership#remove-general-partner-person"),
    REMOVE_GENERAL_PARTNER_LEGAL_ENTITY("limited-partnership#remove-general-partner-legal-entity"),
    REMOVE_LIMITED_PARTNER_PERSON("limited-partnership#remove-limited-partner-person"),
    REMOVE_LIMITED_PARTNER_LEGAL_ENTITY("limited-partnership#remove-limited-partner-legal-entity"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    PartnerKind(String description) {
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
