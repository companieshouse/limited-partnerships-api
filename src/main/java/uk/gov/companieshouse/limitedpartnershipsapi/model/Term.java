package uk.gov.companieshouse.limitedpartnershipsapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Term {
    BY_AGREEMENT,
    UNTIL_DISSOLUTION,
    NONE,
    @JsonEnumDefaultValue
    UNKNOWN;

    @JsonCreator
    public static Term fromName(String name) {
        for (Term term : Term.values()) {
            if (term.name().equals(name)) {
                return term;
            }
        }

        return Term.UNKNOWN;
    }
}
