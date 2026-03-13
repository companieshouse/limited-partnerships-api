package uk.gov.companieshouse.limitedpartnershipsapi.model.psc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum NatureOfControl {
    TEST("test"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    public String getDescription() {
        return description;
    }

    NatureOfControl(String description) {
        this.description = description;
    }

    @JsonCreator
    public static NatureOfControl fromDescription(String description) {
        for (NatureOfControl noc : NatureOfControl.values()) {
            if (noc.getDescription().equalsIgnoreCase(description)) {
                return noc;
            }
        }

        return NatureOfControl.UNKNOWN;
    }
}
