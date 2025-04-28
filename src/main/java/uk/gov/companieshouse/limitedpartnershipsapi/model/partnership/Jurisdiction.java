package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Jurisdiction {
    ENGLAND_AND_WALES("england-wales"),
    NORTHERN_IRELAND("northern-ireland"),
    SCOTLAND("scotland"),

    @JsonEnumDefaultValue
    UNKNOWN("UNKNOWN");

    private final String apiKey;

    Jurisdiction(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @JsonCreator
    public static Jurisdiction fromApiKey(String apiKey) {
        for (Jurisdiction jurisdiction : Jurisdiction.values()) {
            if (jurisdiction.getApiKey().equalsIgnoreCase(apiKey)) {
                return jurisdiction;
            }
        }

        return Jurisdiction.UNKNOWN;
    }
}
