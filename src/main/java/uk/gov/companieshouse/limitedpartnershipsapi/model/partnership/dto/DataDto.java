package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.EnumValid;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.NameSize;

import java.util.List;

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
    @JsonProperty("partnership_number")
    @Pattern(regexp = "^(LP|NL|SL)\\d{6}$", message = "Partnership number must be valid")
    private String partnershipNumber;

    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_name")
    @NotNull(message = "Limited partnership name must not be null")
    @Size(min = MIN_SIZE, message = NAME_MIN_SIZE_MESSAGE)
    @Size(max = LONG_MAX_SIZE, message = NAME_MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Limited partnership name " + INVALID_CHARACTERS_MESSAGE)
    private String partnershipName;

    @JsonInclude(NON_NULL)
    @JsonProperty("name_ending")
    @EnumValid(message = "Name ending must be valid")
    private PartnershipNameEnding nameEnding;

    @JsonProperty("email")
    @Email
    private String email;

    @JsonInclude(NON_NULL)
    @JsonProperty("partnership_type")
    @NotNull(message = "Partnership type must not be null")
    @EnumValid(message = "Partnership type must be valid")
    private PartnershipType partnershipType;

    @JsonInclude(NON_NULL)
    @JsonProperty("jurisdiction")
    @EnumValid(message = "Jurisdiction must be valid")
    private Jurisdiction jurisdiction;

    @JsonProperty("registered_office_address")
    @Valid
    private AddressDto registeredOfficeAddress;

    @JsonProperty("term")
    @EnumValid(message = "Term must be valid")
    private Term term;

    @JsonProperty("principal_place_of_business_address")
    @Valid
    private AddressDto principalPlaceOfBusinessAddress;

    @JsonProperty("sic_codes")
    @Size(min = 1, max = 4, message = "Sic codes list must contain at least 1 sic code, and no more than 4 sic codes")
    private List<@Pattern(regexp = "\\d{5}", message = "Sic code must be 5 numeric characters") String> sicCodes;

    @JsonProperty("lawful_purpose_statement_checked")
    private Boolean lawfulPurposeStatementChecked;

    public String getPartnershipNumber() {
        return partnershipNumber;
    }

    public void setPartnershipNumber(String partnershipNumber) {
        this.partnershipNumber = partnershipNumber;
    }

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
        return jurisdiction != null ? jurisdiction.getApiKey() : null;
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

    public List<String> getSicCodes() {
        return sicCodes;
    }

    public void setSicCodes(List<String> sicCodes) {
        this.sicCodes = sicCodes;
    }

    public Boolean getLawfulPurposeStatementChecked() {
        return lawfulPurposeStatementChecked;
    }

    public void setLawfulPurposeStatementChecked(Boolean lawfulPurposeStatementChecked) {
        this.lawfulPurposeStatementChecked = lawfulPurposeStatementChecked;
    }
}
