package uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

@Document(collection = "persons_with_significant_control")
public class PersonWithSignificantControlDao extends BaseDao {

    @Field("data")
    private PersonWithSignificantControlDataDao data;

    public PersonWithSignificantControlDataDao getData() {
        return data;
    }

    public void setData(PersonWithSignificantControlDataDao data) {
        this.data = data;
    }
}
