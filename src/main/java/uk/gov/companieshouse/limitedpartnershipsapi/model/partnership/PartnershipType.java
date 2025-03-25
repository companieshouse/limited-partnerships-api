package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PartnershipType {
    LP,
    PFLP,
    SLP,
    SPFLP,

    @JsonEnumDefaultValue
    UNKNOWN;

    @JsonCreator
    public static PartnershipType fromValue(String value) {
        try {
            return PartnershipType.valueOf(value);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
