package uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

@Document(collection = "limited_partnership_incorporations")
public class LimitedPartnershipIncorporationDao extends BaseDao {

    @Field("data")
    private IncorporationDataDao data;

    public IncorporationDataDao getData() {
        if (data == null) {
            data = new IncorporationDataDao();
        }
        return data;
    }

    public void setData(IncorporationDataDao data) {
        this.data = data;
    }
}
