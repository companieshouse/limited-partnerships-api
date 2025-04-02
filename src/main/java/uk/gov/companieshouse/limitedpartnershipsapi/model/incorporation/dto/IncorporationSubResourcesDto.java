package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.List;

public class IncorporationSubResourcesDto {

    @JsonProperty("general_partners")
    private List<GeneralPartnerDto> generalPartners;

    @JsonProperty("limited_partners")
    private List<LimitedPartnerDto> limitedPartners;

    @JsonProperty("partnership")
    private LimitedPartnershipDto partnership;

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

    public LimitedPartnershipDto getPartnership() {
        return partnership;
    }

    public void setPartnership(LimitedPartnershipDto partnership) {
        this.partnership = partnership;
    }
}
