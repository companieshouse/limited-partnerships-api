package uk.gov.companieshouse.limitedpartnershipsapi.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class LimitedPartnerDataDto {

    @Field("kind")
    private String kind;

    @Field("etag")
    private String etag;

    @JsonInclude(NON_NULL)
    @JsonProperty("partner_type")
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
