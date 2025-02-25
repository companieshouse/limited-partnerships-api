package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.SHORT_MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MAX_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE_MESSAGE;


public class AddressDto {

    @JsonProperty("address_line_1")
    @NotNull
    @Size(min = MIN_SIZE, message = "address_line_1 " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "address_line_1 " + MAX_SIZE_MESSAGE)
    private String addressLine1;

    @JsonProperty("address_line_2")
    @Size(max = SHORT_MAX_SIZE, message = "address_line_2 " + MAX_SIZE_MESSAGE)
    private String addressLine2;

    @JsonProperty("country")
    @NotNull
    @Size(min = MIN_SIZE, message = "country " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "country " + MAX_SIZE_MESSAGE)
    private String country;

    @JsonProperty("locality")
    @NotNull
    @Size(min = MIN_SIZE, message = "locality " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "locality " + MAX_SIZE_MESSAGE)
    private String locality;

    @JsonProperty("postal_code")
    @NotNull
    @Pattern(regexp = "^[A-Za-z]{1,2}\\d[A-Za-z\\d]? ?\\d[A-Za-z]{2}$")
    @Size(max = 15, message = "postal_code " + MAX_SIZE_MESSAGE)
    private String postalCode;

    @JsonProperty("premises")
    @NotNull
    @Size(min = MIN_SIZE, message = "premises " + MIN_SIZE_MESSAGE)
    @Size(max = SHORT_MAX_SIZE, message = "premises " + MAX_SIZE_MESSAGE)
    private String premises;

    @JsonProperty("region")
    @Size(max = SHORT_MAX_SIZE, message = "region " + MAX_SIZE_MESSAGE)
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
