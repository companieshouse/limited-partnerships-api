package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class IncorporationSubResourcesDto {

    @JsonProperty("general_partners")
    private List<GeneralPartnerDto> generalPartners;

    @JsonProperty("limited_partners")
    private List<LimitedPartnerDto> limitedPartners;

    @JsonProperty("partnership")
    private LimitedPartnershipSubmissionDto partnership;

    public List<GeneralPartnerDto> getGeneralPartners() {
        return generalPartners;
    }

    public void setGeneralPartners(List<GeneralPartnerDto> generalPartners) {
        this.generalPartners = generalPartners;
    }

    public List<LimitedPartnerDto> getLimitedPartners() {
        return limitedPartners;
    }

    public void setLimitedPartners(List<LimitedPartnerDto> limitedPartners) {
        this.limitedPartners = limitedPartners;
    }

    public LimitedPartnershipSubmissionDto getPartnership() {
        return partnership;
    }

    public void setPartnership(LimitedPartnershipSubmissionDto partnership) {
        this.partnership = partnership;
    }
}
