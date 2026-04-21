package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PersonWithSignificantControlType {
    INDIVIDUAL_PERSON,
    RELEVANT_LEGAL_ENTITY,
    OTHER_REGISTRABLE_PERSON,

    @JsonEnumDefaultValue
    UNKNOWN;

    @JsonCreator
    public static PersonWithSignificantControlType fromString(String enumAsString) {
        for (PersonWithSignificantControlType type : PersonWithSignificantControlType.values()) {
            if (type.toString().equalsIgnoreCase(enumAsString)) {
                return type;
            }
        }

        return UNKNOWN;
    }
}
