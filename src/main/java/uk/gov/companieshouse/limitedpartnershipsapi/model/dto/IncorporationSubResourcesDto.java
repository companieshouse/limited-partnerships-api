package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IncorporationSubResourcesDto {

    @JsonProperty("partnership")
    private LimitedPartnershipSubmissionDto partnership;

    public LimitedPartnershipSubmissionDto getPartnership() {
        return partnership;
    }

    public void setPartnership(LimitedPartnershipSubmissionDto partnership) {
        this.partnership = partnership;
    }
}
