package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnershipDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private LimitedPartnershipDataDto data;

    public LimitedPartnershipDataDto getData() {
        return data;
    }

    public void setData(LimitedPartnershipDataDto data) {
        this.data = data;
    }
}
