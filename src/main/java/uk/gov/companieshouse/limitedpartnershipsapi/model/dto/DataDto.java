package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class DataDto {
    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_name")
    private String partnershipName;

    @JsonInclude(NON_NULL)
    @JsonProperty("name_ending")
    private PartnershipNameEnding nameEnding;

    public String getPartnershipName() {
        return partnershipName;
    }

    public void setPartnershipName(String partnershipName) {
        this.partnershipName = partnershipName;
    }

    public PartnershipNameEnding getNameEnding() {
        return nameEnding;
    }

    public void setNameEnding(PartnershipNameEnding nameEnding) {
        this.nameEnding = nameEnding;
    }
}
