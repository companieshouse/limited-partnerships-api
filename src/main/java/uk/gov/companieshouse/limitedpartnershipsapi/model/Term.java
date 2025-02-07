package uk.gov.companieshouse.limitedpartnershipsapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Term {
    DECIDED("decided"),
    DISSOLVED("dissolved"),
    NONE("none"),
    @JsonEnumDefaultValue
    UNKNOWN("Unknown");

    private final String description;

    Term(String description) {
        this.description = description;
    }

    @JsonCreator
    public static Term fromDescription(String description) {
        for (Term term : Term.values()) {
            if (term.getDescription().equalsIgnoreCase(description)) {
                return term;
            }
        }

        return Term.UNKNOWN;
    }

    public String getDescription() {
        return description;
    }
}
