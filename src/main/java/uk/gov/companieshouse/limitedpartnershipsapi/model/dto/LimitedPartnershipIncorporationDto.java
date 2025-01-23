package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnershipIncorporationDto {

    @JsonProperty("kind")
    private String kind;

    @JsonInclude(NON_NULL)
    @JsonProperty("sub_resources")
    private IncorporationSubResourcesDto subResources;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public IncorporationSubResourcesDto getSubResources() {
        return subResources;
    }

    public void setSubResources(IncorporationSubResourcesDto subResources) {
        this.subResources = subResources;
    }
}
