package uk.gov.companieshouse.limitedpartnershipsapi.model.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class IncorporationDataDao {

    @Field("kind")
    private String kind;

    @Field("etag")
    private String etag;

    public IncorporationDataDao() {
        // used by Spring MongoDB
    }

    public IncorporationDataDao(Builder builder) {
        this.kind = builder.kind;
        this.etag = builder.etag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() { return etag; }

    public void setEtag(String etag) { this.etag = etag; }

    public static class Builder {
        private String kind;
        private String etag;

        public Builder setKind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder setEtag(String etag) {
            this.etag = etag;
            return this;
        }

        public IncorporationDataDao build() {
            return new IncorporationDataDao(this);
        }
    }
}
