package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MAX_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MAX_SIZE_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.MIN_SIZE_MESSAGE;

public class AddressDto {
    @JsonProperty("address_line_1")
    @Size(min = MIN_SIZE, message = "address_line_1 " + MIN_SIZE_MESSAGE)
    @Size(max = MAX_SIZE, message = "address_line_1 " + MAX_SIZE_MESSAGE)
    private String addressLine1;

    @JsonProperty("address_line_2")
    @Size(max = MAX_SIZE, message = "address_line_2 " + MAX_SIZE_MESSAGE)
    private String addressLine2;

    @JsonProperty("country")
    @Size(min = MIN_SIZE, message = "country " + MIN_SIZE_MESSAGE)
    @Size(max = MAX_SIZE, message = "country " + MAX_SIZE_MESSAGE)
    private String country;

    @JsonProperty("locality")
    @Size(min = MIN_SIZE, message = "locality " + MIN_SIZE_MESSAGE)
    @Size(max = MAX_SIZE, message = "locality " + MAX_SIZE_MESSAGE)
    private String locality;

    @JsonProperty("postal_code")
    @Pattern(regexp = "^[A-Za-z]{1,2}\\d[A-Za-z\\d]? ?\\d[A-Za-z]{2}$")
    @Size(max = 15, message = "postal_code " + MAX_SIZE_MESSAGE)
    private String postalCode;

    @JsonProperty("premises")
    @Size(min = MIN_SIZE, message = "premises " + MIN_SIZE_MESSAGE)
    @Size(max = MAX_SIZE, message = "premises " + MAX_SIZE_MESSAGE)
    private String premises;

    @JsonProperty("region")
    @Size(max = MAX_SIZE, message = "region " + MAX_SIZE_MESSAGE)
    private String region;
}
