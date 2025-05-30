package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.PartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;

public class LimitedPartnerDataDao extends PartnerDataDao {

    // Legal Entity

    @Field("contribution_currency_type")
    private Currency contributionCurrencyType;

    @Field("contribution_currency_value")
    private String contributionCurrencyValue;

    @Field("contribution_non_monetary_value")
    private String contributionNonMonetaryValue;

    public Currency getContributionCurrencyType() {
        return contributionCurrencyType;
    }

    public void setContributionCurrencyType(Currency contributionCurrencyType) {
        this.contributionCurrencyType = contributionCurrencyType;
    }

    public String getContributionCurrencyValue() {
        return contributionCurrencyValue;
    }

    public void setContributionCurrencyValue(String contributionCurrencyValue) {
        this.contributionCurrencyValue = contributionCurrencyValue;
    }

    public String getContributionNonMonetaryValue() {
        return contributionNonMonetaryValue;
    }

    public void setContributionNonMonetaryValue(String contributionNonMonetaryValue) {
        this.contributionNonMonetaryValue = contributionNonMonetaryValue;
    }
}