package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;

public class LimitedPartnershipPatchDto {
    @JsonProperty("partnership_name")
    private JsonNullable<String> partnershipName;

    @JsonProperty("name_ending")
    private JsonNullable<PartnershipNameEnding> nameEnding;

    @JsonProperty("email")
    private JsonNullable<String> email;

    @JsonProperty("partnership_type")
    private JsonNullable<PartnershipType> partnershipType;

    public JsonNullable<String> getPartnershipName() {
        return partnershipName;
    }

    public void setPartnershipName(JsonNullable<String> partnershipName) {
        this.partnershipName = partnershipName;
    }

    public JsonNullable<PartnershipNameEnding> getNameEnding() {
        return nameEnding;
    }

    public void setNameEnding(JsonNullable<PartnershipNameEnding> nameEnding) {
        this.nameEnding = nameEnding;
    }

    public JsonNullable<String> getEmail() {
        return email;
    }

    public void setEmail(JsonNullable<String> email) {
        this.email = email;
    }

    public JsonNullable<PartnershipType> getPartnershipType() {
        return partnershipType;
    }

    public void setPartnershipType(JsonNullable<PartnershipType> partnershipType) {
        this.partnershipType = partnershipType;
    }
}
