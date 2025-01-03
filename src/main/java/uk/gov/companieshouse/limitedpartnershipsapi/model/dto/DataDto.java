package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.NameSize;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@NameSize
public class DataDto {
    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_name")
    @Size(min = 1, message = "partnership_name must be greater than 1")
    @Size(max = 160, message = "partnership_name must be less than 160")
    private String partnershipName;

    @JsonInclude(NON_NULL)
    @JsonProperty("name_ending")
    private PartnershipNameEnding nameEnding;

    @JsonProperty("email")
    @Email
    private String email;

    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_type")
    private PartnershipType partnershipType;

    public String getPartnershipName() {
        return partnershipName;
    }

    public void setPartnershipName(String partnershipName) {
        this.partnershipName = partnershipName;
    }

    public String getNameEnding() {
        return nameEnding != null ? nameEnding.getDescription() : null;
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
}
