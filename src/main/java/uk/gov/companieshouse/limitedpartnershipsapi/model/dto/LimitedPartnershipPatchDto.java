package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LimitedPartnershipPatchDto {
    @JsonProperty("data")
    private PatchDto data;

    public PatchDto getData() {
        return data;
    }

    public void setData(PatchDto data) {
        this.data = data;
    }
}
