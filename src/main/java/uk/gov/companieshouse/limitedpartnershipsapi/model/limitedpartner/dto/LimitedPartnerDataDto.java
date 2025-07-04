package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.PartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.validator.EnumValid;

import java.util.List;

public class LimitedPartnerDataDto extends PartnerDataDto {

    // Legal Entity
    public static final String CONTRIBUTION_CURRENCY_TYPE_FIELD = "contribution_currency_type";
    public static final String CONTRIBUTION_CURRENCY_VALUE_FIELD = "contribution_currency_value";
    public static final String CONTRIBUTION_SUB_TYPES_FIELD = "contribution_sub_types";

    @JsonProperty(CONTRIBUTION_CURRENCY_TYPE_FIELD)
    @EnumValid(message = "Contribution currency type must be valid")
    private Currency contributionCurrencyType;

    @JsonProperty(CONTRIBUTION_CURRENCY_VALUE_FIELD)
    private String contributionCurrencyValue;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty(CONTRIBUTION_SUB_TYPES_FIELD)
    @EnumValid(message = "Capital contribution type must be valid")
    private List<ContributionSubTypes> contributionSubTypes;

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

    public List<ContributionSubTypes> getContributionSubTypes() {
        return contributionSubTypes;
    }

    public void setContributionSubTypes(List<ContributionSubTypes> contributionSubTypes) {
        this.contributionSubTypes = contributionSubTypes;
    }
}
