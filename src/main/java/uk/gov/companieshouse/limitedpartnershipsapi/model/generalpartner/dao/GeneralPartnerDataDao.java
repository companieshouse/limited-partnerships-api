package uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.PartnerDataDao;

public class GeneralPartnerDataDao extends PartnerDataDao {

    // Person

    @Field("not_disqualified_statement_checked")
    private Boolean notDisqualifiedStatementChecked;

    public Boolean getNotDisqualifiedStatementChecked() {
        return notDisqualifiedStatementChecked;
    }

    public void setNotDisqualifiedStatementChecked(Boolean notDisqualifiedStatementChecked) {
        this.notDisqualifiedStatementChecked = notDisqualifiedStatementChecked;
    }
}
