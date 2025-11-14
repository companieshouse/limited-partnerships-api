package uk.gov.companieshouse.limitedpartnershipsapi.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum FilingMode {
    REGISTRATION("limited-partnership-registration"),
    TRANSITION("limited-partnership-transition"),
    POST_TRANSITION("limited-partnership-post-transition"),
    DEFAULT("default"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    FilingMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static FilingMode fromDescription(String description) {
        for (FilingMode kind : FilingMode.values()) {
            if (kind.getDescription().equalsIgnoreCase(description)) {
                return kind;
            }
        }

        return FilingMode.UNKNOWN;
    }
}
