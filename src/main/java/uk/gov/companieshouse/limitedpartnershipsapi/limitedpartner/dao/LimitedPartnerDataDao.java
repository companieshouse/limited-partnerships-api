package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.enums.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.limitedpartner.enums.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.dao.PartnerDataDao;

import java.util.List;

public class LimitedPartnerDataDao extends PartnerDataDao {

    // Legal Entity

    @Field("contribution_currency_type")
    private Currency contributionCurrencyType;

    @Field("contribution_currency_value")
    private String contributionCurrencyValue;

    @Field("contribution_sub_types")
    private List<ContributionSubTypes> contributionSubTypes;

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

    public List<ContributionSubTypes> getContributionSubTypes() {
        return contributionSubTypes;
    }

    public void setContributionSubTypes(List<ContributionSubTypes> contributionSubTypes) {
        this.contributionSubTypes = contributionSubTypes;
    }
}
