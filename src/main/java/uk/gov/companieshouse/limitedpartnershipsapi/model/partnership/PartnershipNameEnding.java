package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PartnershipNameEnding {
    LIMITED_PARTNERSHIP("Limited Partnership"),
    LP("LP"),
    L_DOT_P_DOT("L.P."),
    PARTNERIAETH_CYFYNGEDIG("Partneriaeth Cyfyngedig"),
    PC("PC"),
    P_DOT_C_DOT("P.C.");

    private final String description;

    PartnershipNameEnding(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static PartnershipNameEnding fromDescription(String description) {
        for (PartnershipNameEnding nameEnding : PartnershipNameEnding.values()) {
            if (nameEnding.getDescription().equalsIgnoreCase(description)) {
                return nameEnding;
            }
        }
        throw new IllegalArgumentException(
                "No PartnershipNameEnding constant with description " + description);
    }
}
