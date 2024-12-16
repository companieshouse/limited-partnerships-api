package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchDto {

    @JsonProperty("partnership_name")
    private JsonNullable<String> partnershipName;

    public JsonNullable<String> getPartnershipName() {
        return partnershipName;

    }

    public void setPartnershipName(JsonNullable<String> partnershipName) {
        this.partnershipName = partnershipName;
    }

    @JsonProperty("name_ending")
    private PartnershipNameEnding nameEnding;

    @JsonProperty("email")
    private String email;


    public PartnershipNameEnding getNameEnding() {
        return nameEnding;
    }

    public void setNameEnding(PartnershipNameEnding nameEnding) {
        this.nameEnding = nameEnding;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
