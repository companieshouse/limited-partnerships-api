package uk.gov.companieshouse.limitedpartnershipsapi.model.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class IncorporationDataDao {

    @Field("kind")
    private String kind;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
