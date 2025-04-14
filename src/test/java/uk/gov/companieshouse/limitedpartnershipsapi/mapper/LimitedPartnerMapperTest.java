package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LimitedPartnerMapperTest {

    private final LimitedPartnerMapper mapper = Mappers.getMapper(LimitedPartnerMapper.class);

    @Test
    void limitedPartnerNationalityEnumToString() {
        Nationality sourceData = Nationality.ENGLISH;

        String destinationData = mapper.mapNationalityToString(sourceData);

        assertEquals(sourceData.getDescription().toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void limitedPartnerNationalityStringToEnum() {

        String sourceData = Nationality.ENGLISH.getDescription();

        Nationality destinationData = mapper.mapNationalityToEnum(sourceData);

        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void limitedPartnerCountryEnumToString() {
        Country sourceData = Country.ENGLAND;

        String destinationData = mapper.mapCountryToString(sourceData);

        assertEquals(sourceData.getDescription().toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void limitedPartnerCountryStringToEnum() {

        String sourceData = Country.ENGLAND.getDescription();

        Country destinationData = mapper.mapCountryToEnum(sourceData);

        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void limitedPartnerCurrencyEnumToString() {
        Currency sourceData = Currency.ENGLAND;

        String destinationData = mapper.mapCurrencyToString(sourceData);

        assertEquals(sourceData.getDescription().toLowerCase(), destinationData.toLowerCase());

    }

    @Test
    void limitedPartnerCurrencyStringToEnum() {

        String sourceData = Currency.ENGLAND.getDescription();

        Currency destinationData = mapper.mapCurrencyToEnum(sourceData);

        assertEquals(sourceData, destinationData.getDescription());
    }
}
