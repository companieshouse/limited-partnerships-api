package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.EnumValid;

public class LimitedPartnerDataDto extends PartnerDataDto {

    // Legal Entity

    @JsonProperty("contribution_currency_type")
    @EnumValid
    private Currency contributionCurrencyType;

    @JsonProperty("contribution_currency_value")
    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "Value must be a valid decimal number")
    private String contributionCurrencyValue;

    @JsonProperty("contribution_non_monetary_value")
    private String contributionNonMonetaryValue;

    public boolean isLegalEntity() {
        return getLegalEntityRegisterName() != null || getLegalForm() != null;
    }

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
