package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

@Document(collection = "limited_partners")
public class LimitedPartnerDao extends BaseDao {

    @Field("data")
    private LimitedPartnerDataDao data;

    public LimitedPartnerDataDao getData() {
        if (data == null) {
            data = new LimitedPartnerDataDao();
        }
        return data;
    }

    public void setData(LimitedPartnerDataDao data) {
        this.data = data;
    }
}
