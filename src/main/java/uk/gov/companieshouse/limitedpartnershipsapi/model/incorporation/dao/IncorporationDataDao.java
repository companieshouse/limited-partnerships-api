package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class IncorporationDataDao {

    @Field("kind")
    private String kind;

    @Field("etag")
    private String etag;

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

}
