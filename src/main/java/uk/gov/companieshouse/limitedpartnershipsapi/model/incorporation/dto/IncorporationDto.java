package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class IncorporationDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private IncorporationDataDto data;

    public IncorporationDataDto getData() {
        return data;
    }

    public void setData(IncorporationDataDto data) {
        this.data = data;
    }

}
