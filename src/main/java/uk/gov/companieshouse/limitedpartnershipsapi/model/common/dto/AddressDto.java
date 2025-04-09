package uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.UkPostcode;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MAX_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.REG_EXP_FOR_ALLOWED_CHARACTERS;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SHORT_MAX_SIZE;

@UkPostcode
public class AddressDto {

    @JsonProperty("address_line_1")
    @NotNull(message = "Address line 1 must not be null")
    @Size(min = MIN_SIZE, message = "Address line 1 " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "Address line 1 " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Address line 1 " + INVALID_CHARACTERS_MESSAGE)
    private String addressLine1;

    @JsonProperty("address_line_2")
    @Size(max = SHORT_MAX_SIZE, message = "Address line 2 " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Address line 2 " + INVALID_CHARACTERS_MESSAGE)
    private String addressLine2;

    @JsonProperty("country")
    @NotNull(message = "Country must not be null")
    @Size(min = MIN_SIZE, message = "country " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "country " + MAX_SIZE_MESSAGE)
    private String country;

    @JsonProperty("locality")
    @NotNull(message = "Town or city must not be null")
    @Size(min = MIN_SIZE, message = "Town or city " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "Town or city " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Town or city " + INVALID_CHARACTERS_MESSAGE)
    private String locality;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("premises")
    @NotNull(message = "Property name or number must not be null")
    @Size(min = MIN_SIZE, message = "Property name or number " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "Property name or number " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "Property name or number " + INVALID_CHARACTERS_MESSAGE)
    private String premises;

    @JsonProperty("region")
    @Size(max = SHORT_MAX_SIZE, message = "County " + MAX_SIZE_MESSAGE)
    @Pattern(regexp = REG_EXP_FOR_ALLOWED_CHARACTERS, message = "County " + INVALID_CHARACTERS_MESSAGE)
    private String region;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPremises() {
        return premises;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
