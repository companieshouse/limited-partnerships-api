package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.NameSize;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.ValidJurisdiction;

@NameSize
public class LimitedPartnershipPatchDto {
    @JsonProperty("partnership_name")
    @Size(min = DataDto.NAME_MIN_SIZE, message = DataDto.NAME_MIN_SIZE_MESSAGE)
    @Size(max = DataDto.NAME_MAX_SIZE, message = DataDto.NAME_MAX_SIZE_MESSAGE)
    private String partnershipName;

    @JsonProperty("name_ending")
    private PartnershipNameEnding nameEnding;

    @JsonProperty("email")
    @Email
    private String email;

    @JsonProperty("partnership_type")
    private PartnershipType partnershipType;

    @JsonProperty("jurisdiction")
    @ValidJurisdiction
    private Jurisdiction jurisdiction;


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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PartnershipType getPartnershipType() {
        return partnershipType;
    }

    public void setPartnershipType(PartnershipType partnershipType) {
        this.partnershipType = partnershipType;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }
}
