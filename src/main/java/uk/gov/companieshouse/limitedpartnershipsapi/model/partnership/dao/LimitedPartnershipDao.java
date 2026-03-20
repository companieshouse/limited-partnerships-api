package uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

@Document(collection = "limited_partnerships")
public class LimitedPartnershipDao extends BaseDao {

    @Field("data")
    private DataDao data;

    public DataDao getData() {
        return data;
    }

    public void setData(DataDao data) {
        this.data = data;
    }
}
