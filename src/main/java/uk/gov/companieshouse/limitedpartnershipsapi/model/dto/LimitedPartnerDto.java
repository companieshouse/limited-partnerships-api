package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnerDto {
    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private LimitedPartnerDataDto data;

    public LimitedPartnerDataDto getData() {
        return data;
    }

    public void setData(LimitedPartnerSubmissionDto data) {
        this.data = data.getData();
    }

}
