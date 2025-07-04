package uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressBuilder.addressBuilder;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes.SHARES;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;

public class LimitedPartnerDataDtoBuilder {

    private BigDecimal contributionCurrencyValue;

    public static LimitedPartnerDataDtoBuilder limitedPartnerDataDtoBuilder() {
        return new LimitedPartnerDataDtoBuilder();
    }

    public LimitedPartnerDataDtoBuilder withContributionCurrencyValue(BigDecimal contributionValue) {
        this.contributionCurrencyValue = contributionValue;
        return this;
    }

    public LimitedPartnerDataDto build() {
        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setForename("Jack");
        dataDto.setSurname("Jones");
        dataDto.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDto.setNationality1(Nationality.EMIRATI);
        dataDto.setContributionSubTypes(List.of(SHARES));
        dataDto.setContributionCurrencyValue(contributionCurrencyValue);
        dataDto.setUsualResidentialAddress(addressBuilder().buildDto());
        return dataDto;
    }
}
