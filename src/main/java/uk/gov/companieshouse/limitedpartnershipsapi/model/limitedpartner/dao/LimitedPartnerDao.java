package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

@Document(collection = "limited_partners")
public class LimitedPartnerDao extends BaseDao {

    @Field("data")
    private LimitedPartnerDataDao data;

    @Field("transaction_id")
    private String transactionId;

    public LimitedPartnerDataDao getData() {
        if (data == null) {
            data = new LimitedPartnerDataDao();
        }
        return data;
    }

    public void setData(LimitedPartnerDataDao data) {
        this.data = data;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
