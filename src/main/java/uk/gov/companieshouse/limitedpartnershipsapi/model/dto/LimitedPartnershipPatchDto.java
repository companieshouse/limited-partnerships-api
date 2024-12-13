package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
