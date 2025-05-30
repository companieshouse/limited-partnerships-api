package uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.PartnerDataDao;

public class GeneralPartnerDataDao extends PartnerDataDao {

    // Person

    @Field("service_address")
    private AddressDao serviceAddress;

    public AddressDao getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(AddressDao serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    // Legal Entity

    @Field("not_disqualified_statement_checked")
    private boolean notDisqualifiedStatementChecked;

    public boolean getNotDisqualifiedStatementChecked() {
        return notDisqualifiedStatementChecked;
    }

    public void setNotDisqualifiedStatementChecked(boolean notDisqualifiedStatementChecked) {
        this.notDisqualifiedStatementChecked = notDisqualifiedStatementChecked;
    }
}
