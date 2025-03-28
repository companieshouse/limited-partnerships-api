package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Jurisdiction {
    ENGLAND_AND_WALES("England and Wales"),
    NORTHERN_IRELAND("Northern Ireland"),
    SCOTLAND("Scotland"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String description;

    Jurisdiction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static Jurisdiction fromDescription(String description) {
        for (Jurisdiction jurisdiction : Jurisdiction.values()) {
            if (jurisdiction.getDescription().equalsIgnoreCase(description)) {
                return jurisdiction;
            }
        }

        return Jurisdiction.UNKNOWN;
    }
}
