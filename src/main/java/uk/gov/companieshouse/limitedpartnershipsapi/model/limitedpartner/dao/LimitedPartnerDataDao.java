package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.LimitedPartnerType;

public class LimitedPartnerDataDao {
    @Field("kind")
    private String kind;

    @Field("etag")
    private String etag;

    @Field("limited_partner_type")
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
