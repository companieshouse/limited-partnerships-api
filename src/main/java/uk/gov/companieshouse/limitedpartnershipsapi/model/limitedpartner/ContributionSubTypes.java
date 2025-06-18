package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ContributionSubTypes {
    MONEY,
    LAND_OR_PROPERTY,
    SHARES,
    SERVICES_OR_GOODS,
    ANY_OTHER_ASSET,

    @JsonEnumDefaultValue
    UNKNOWN;

    @JsonCreator
    public static ContributionSubTypes fromName(String code) {
        for (ContributionSubTypes subType : ContributionSubTypes.values()) {
            if (subType.name().equals(code)) {
                return subType;
            }
        }

        return ContributionSubTypes.UNKNOWN;
    }

}
