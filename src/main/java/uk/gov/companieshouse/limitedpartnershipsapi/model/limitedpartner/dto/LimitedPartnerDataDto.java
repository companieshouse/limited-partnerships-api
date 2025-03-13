package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.LimitedPartnerType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnerDataDto {

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("etag")
    private String etag;

    @JsonInclude(NON_NULL)
    @JsonProperty("limited_partner_type")
    private LimitedPartnerType partnerType;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public LimitedPartnerType getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(LimitedPartnerType partnerType) {
        this.partnerType = partnerType;
    }
}
