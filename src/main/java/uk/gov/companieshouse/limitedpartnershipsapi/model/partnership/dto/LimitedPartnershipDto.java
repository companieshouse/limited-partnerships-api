package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnershipDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private DataDto data;

    public DataDto getData() {
        return data;
    }

    public void setData(DataDto data) {
        this.data = data;
    }
}
