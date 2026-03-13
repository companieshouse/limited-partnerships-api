package uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.BaseDao;

@Document(collection = "general_partners")
public class GeneralPartnerDao extends BaseDao {

    @Field("data")
    private GeneralPartnerDataDao data;

    @Field("transaction_id")
    private String transactionId;

    public GeneralPartnerDataDao getData() {
        return data;
    }

    public void setData(GeneralPartnerDataDao data) {
        this.data = data;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
