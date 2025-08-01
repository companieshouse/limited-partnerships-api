package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum IncorporationKind {
    REGISTRATION("limited-partnership-registration"),
    TRANSITION("limited-partnership-transition"),
    POST_TRANSITION("limited-partnership-post-transition"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

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
