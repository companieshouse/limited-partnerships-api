package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum PersonWithSignificantControlType {
    INDIVIDUAL_PERSON,
    RELEVANT_LEGAL_ENTITY,
    OTHER_REGISTRABLE_PERSON,

    @JsonEnumDefaultValue
    UNKNOWN
}
