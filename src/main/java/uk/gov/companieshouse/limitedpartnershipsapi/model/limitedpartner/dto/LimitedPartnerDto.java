package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnerDto extends PartnerDto {

    @JsonInclude(NON_NULL)
    @JsonProperty("data")
    @Valid
    private LimitedPartnerDataDto data;
    
    public LimitedPartnerDataDto getData() {
        return data;
    }

    public void setData(LimitedPartnerDataDto data) {
        this.data = data;
    }


}