package uk.gov.companieshouse.limitedpartnershipsapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum IncorporationKind {
    REGISTRATION("limited-partnership-registration"),
    TRANSITION("limited-partnership-transition"),
    @JsonEnumDefaultValue
    UNKNOWN("Unknown");

    private final String description;

    IncorporationKind(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static IncorporationKind fromDescription(String description) {
        for (IncorporationKind kind : IncorporationKind.values()) {
            if (kind.getDescription().equalsIgnoreCase(description)) {
                return kind;
            }
        }

        return IncorporationKind.UNKNOWN;
    }
}
