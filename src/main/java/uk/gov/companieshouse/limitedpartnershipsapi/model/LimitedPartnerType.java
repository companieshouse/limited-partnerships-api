package uk.gov.companieshouse.limitedpartnershipsapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LimitedPartnerType {
    person("person"),
    LEGAL_ENTITY("legal_entity");

    private final String description;

    LimitedPartnerType(String description) {
        this.description = description;
    }

    @JsonCreator
    public static LimitedPartnerType fromDescription(String description) {
        for (LimitedPartnerType partnerType : LimitedPartnerType.values()) {
            if (partnerType.getDescription().equalsIgnoreCase(description)) {
                return partnerType;
            }
        }
        throw new IllegalArgumentException(
                "No LimitedPartnerType constant with description " + description);
    }

    public String getDescription() {
        return description;
    }
}
