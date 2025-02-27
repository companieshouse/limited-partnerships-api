package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Nationality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeneralPartnerMapperTest {

    private final GeneralPartnerMapper mapper = Mappers.getMapper(GeneralPartnerMapper.class);

    @Test
    void mapNationalityEnumToString() {
        Nationality sourceData = Nationality.BELGIAN;
        String destinationData = mapper.mapNationalityEnumToString(sourceData);
        assertEquals(sourceData.toString().toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void mapDoubleBarreledNationalityEnumToString() {
        Nationality sourceData = Nationality.NEW_ZEALANDER;
        String destinationData = mapper.mapNationalityEnumToString(sourceData);
        assertEquals(sourceData.toString().replace('_', ' ').toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void mapValidStringToEnum() {
        String sourceData = "Belgian";
        Nationality destinationData = mapper.mapStringToNationalityEnum(sourceData);
        assertEquals(sourceData.toLowerCase(), destinationData.toString().toLowerCase());
    }

    @Test
    void mapValidDoubleBarreledStringToEnum() {
        String sourceData = "New Zealander";
        Nationality destinationData = mapper.mapStringToNationalityEnum(sourceData);
        assertEquals(sourceData.toLowerCase(), destinationData.toString().replace('_', ' ').toLowerCase());
    }

    @Test
    void mapInvalidStringToEnum() {
        String sourceData = "Rude";
        Nationality destinationData = mapper.mapStringToNationalityEnum(sourceData);
        assertEquals(Nationality.UNKNOWN.toString().toLowerCase(), destinationData.toString().toLowerCase());
    }

    @Test
    void attemptMapNullToEnumAndGetNull() {
        String sourceData = null;
        Nationality destinationData = mapper.mapStringToNationalityEnum(sourceData);
        assertNull(destinationData);
    }
}
