package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.LimitedPartnerType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
