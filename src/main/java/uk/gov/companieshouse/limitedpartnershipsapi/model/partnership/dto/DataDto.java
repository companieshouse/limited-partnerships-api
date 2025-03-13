package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.NameSize;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.ValidEnum;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LONG_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.REG_EXP_FOR_ALLOWED_CHARACTERS;

@NameSize
public class DataDto {

    public static final String NAME_MIN_SIZE_MESSAGE = "Limited partnership name must be greater than {min}";
    public static final String NAME_MAX_SIZE_MESSAGE = "Limited partnership name must be less than {max}";

    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_name")
    @NotNull(message = "Enter the name of the limited partnership")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Limited partnership name " + INVALID_CHARACTERS_MESSAGE)
    private String partnershipName;

    @JsonInclude(NON_NULL)
    @JsonProperty("name_ending")
    @NotNull(message = "Select the name ending to go on the public record")
    private PartnershipNameEnding nameEnding;

    @JsonProperty("email")
    @Email
    private String email;

    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_type")
    @NotNull(message = "Partnership type must not be null")
    private PartnershipType partnershipType;

    @JsonInclude(NON_NULL)
    @JsonProperty("jurisdiction")
    @ValidEnum(message = "Jurisdiction must be valid")
    private Jurisdiction jurisdiction;

    @JsonProperty("registered_office_address")
    @Valid
    private AddressDto registeredOfficeAddress;

    @JsonProperty("term")
    @ValidEnum(message = "Term must be valid")
    private Term term;

    @JsonProperty("principal_place_of_business_address")
    @Valid
    private AddressDto principalPlaceOfBusinessAddress;

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

    public String getJurisdiction() {
        return jurisdiction != null ? jurisdiction.getDescription() : null;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public AddressDto getRegisteredOfficeAddress() {
        return registeredOfficeAddress;
    }

    public void setRegisteredOfficeAddress(AddressDto registeredOfficeAddress) {
        this.registeredOfficeAddress = registeredOfficeAddress;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public AddressDto getPrincipalPlaceOfBusinessAddress() {
        return principalPlaceOfBusinessAddress;
    }

    public void setPrincipalPlaceOfBusinessAddress(AddressDto principalPlaceOfBusinessAddress) {
        this.principalPlaceOfBusinessAddress = principalPlaceOfBusinessAddress;
    }
}
