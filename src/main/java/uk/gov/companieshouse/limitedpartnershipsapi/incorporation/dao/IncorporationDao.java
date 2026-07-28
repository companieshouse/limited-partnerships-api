package uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dao.BaseDao;

@Document(collection = "limited_partnership_incorporations")
public class IncorporationDao extends BaseDao {

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
