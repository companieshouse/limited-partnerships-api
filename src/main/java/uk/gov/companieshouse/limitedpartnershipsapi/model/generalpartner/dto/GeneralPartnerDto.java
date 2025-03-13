package uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class GeneralPartnerDto {
    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private GeneralPartnerDataDto data;

    public GeneralPartnerDataDto getData() {
        return data;
    }

    public void setData(GeneralPartnerDataDto data) {
        this.data = data;
    }
}
