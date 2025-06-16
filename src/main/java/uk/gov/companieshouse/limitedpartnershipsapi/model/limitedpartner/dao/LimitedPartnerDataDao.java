package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.PartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;

import java.util.List;

public class LimitedPartnerDataDao extends PartnerDataDao {

    // Legal Entity

    @Field("contribution_currency_type")
    private Currency contributionCurrencyType;

    @Field("contribution_currency_value")
    private String contributionCurrencyValue;

    @Field("contribution_sub_types")
    private List<String> contributionSubTypes;

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

    public List<String> getContributionSubTypes() {
        return contributionSubTypes;
    }

    public void setContributionSubTypes(List<String> contributionSubTypes) {
        this.contributionSubTypes = contributionSubTypes;
    }
}