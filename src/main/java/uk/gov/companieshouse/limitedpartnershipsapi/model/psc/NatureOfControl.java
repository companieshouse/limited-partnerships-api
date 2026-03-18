package uk.gov.companieshouse.limitedpartnershipsapi.model.psc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum NatureOfControl {
    INDIVIDUAL("Nature of control for this individual"),
    INDIVIDUAL_FIRM_CONTROL("Nature of control by a firm over which this individual has significant control"),
    INDIVIDUAL_TRUST_CONTROL("Nature of control by a trust over which this individual has significant control"),
    RLE("Nature of control for this relevant legal entity (RLE)"),
    RLE_FIRM_CONTROL("Nature of control by a firm over which the RLE has significant control"),
    RLE_TRUST_CONTROL("Nature of control by a trust over which the RLE has significant control"),
    ORP("Nature of control for this other registrable person (ORP)"),
    ORP_FIRM_CONTROL("Nature of control by a firm over which the ORP has significant control"),
    ORP_TRUST_CONTROL("Nature of control by a trust over which the ORP has significant control"),

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
