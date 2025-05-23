package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnerDto {

    private String id;

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private LimitedPartnerDataDto data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LimitedPartnerDataDto getData() {
        return data;
    }

    public void setData(LimitedPartnerDataDto data) {
        this.data = data;
    }

}