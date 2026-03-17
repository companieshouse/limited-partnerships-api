package uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;


@Document(collection = "limited_partnership_pscs")
public class PscDao extends BaseDao {

    @Field("data")
    private PscDataDao data;

    @Field("transaction_id")
    private String transactionId;

    public PscDataDao getData() {
        return data;
    }

    public void setData(PscDataDao data) {
        this.data = data;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
