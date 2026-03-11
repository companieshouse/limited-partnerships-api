package uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class PscDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    private PscDataDto data;

    public PscDataDto getData() {
        return data;
    }

    public void setData(PscDataDto data) {
        this.data = data;
    }
}
