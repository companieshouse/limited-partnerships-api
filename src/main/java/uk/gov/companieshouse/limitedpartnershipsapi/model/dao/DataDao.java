package uk.gov.companieshouse.limitedpartnershipsapi.model.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class DataDao {

    @Field("partnership_name")
    private String partnershipName;

    @Field("name_ending")
    private String nameEnding;

    public String getPartnershipName() {
        return partnershipName;
    }

    public void setPartnershipName(String partnershipName) {
        this.partnershipName = partnershipName;
    }

    public String getNameEnding() {
        return nameEnding;
    }

    public void setNameEnding(String nameEnding) {
        this.nameEnding = nameEnding;
    }
}
