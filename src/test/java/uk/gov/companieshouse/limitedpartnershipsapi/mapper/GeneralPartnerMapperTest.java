package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralPartnerMapperTest {

    private final GeneralPartnerMapper mapper = Mappers.getMapper(GeneralPartnerMapper.class);

    @Test
    void generalPartnerNationalityEnumToString() {
        Nationality sourceData = Nationality.ENGLISH;

        String destinationData = mapper.mapNationalityToString(sourceData);

        assertEquals(sourceData.getDescription().toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void generalPartnerNationalityStringToEnum() {

        String sourceData = Nationality.ENGLISH.getDescription();

        Nationality destinationData = mapper.mapNationalityToEnum(sourceData);

        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void generalPartnerCountryEnumToString() {
        Country sourceData = Country.ENGLAND;

        String destinationData = mapper.mapCountryToString(sourceData);

        assertEquals(sourceData.getDescription().toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void generalPartnerCountryStringToEnum() {

        String sourceData = Country.ENGLAND.getDescription();

        Country destinationData = mapper.mapCountryToEnum(sourceData);

        assertEquals(sourceData, destinationData.getDescription());
    }

}
