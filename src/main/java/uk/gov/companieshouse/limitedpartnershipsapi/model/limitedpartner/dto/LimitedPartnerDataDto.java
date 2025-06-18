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

    @JsonProperty("contribution_currency_type")
    @EnumValid(message = "Contribution currency must be valid type")
    private Currency contributionCurrencyType;

    @JsonProperty("contribution_currency_value")
    @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "Value must be a valid decimal number")
    private String contributionCurrencyValue;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("contribution_sub_types")
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
