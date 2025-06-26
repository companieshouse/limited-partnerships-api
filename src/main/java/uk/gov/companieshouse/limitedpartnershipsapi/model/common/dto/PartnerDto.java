package uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public abstract class PartnerDto {
    protected String id;

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private PartnerDataDto data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PartnerDataDto getData() {
        return data;
    }

    public void setData(PartnerDataDto data) {
        this.data = data;
    }
}
