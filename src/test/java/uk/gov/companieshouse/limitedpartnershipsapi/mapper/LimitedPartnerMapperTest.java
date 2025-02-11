package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimitedPartnerMapperTest {

    private final LimitedPartnerMapper mapper = Mappers.getMapper(LimitedPartnerMapper.class);

    @Test
    void givenPartnerTypeEnum_whenMapsToString_thenCorrect() {
        // given
        LimitedPartnerType sourceData = LimitedPartnerType.LEGAL_ENTITY;
        // when
        String destinationData = LimitedPartnerMapper.INSTANCE.mapPartnerTypeToString(sourceData);
        // then
        assertEquals(sourceData.getDescription().toLowerCase(), destinationData.toLowerCase());
    }

    @Test
    void givenPartnerTypeString_whenMapsToENum_thenCorrect() {
        // given
        String sourceData = LimitedPartnerType.LEGAL_ENTITY.getDescription();
        // when
        LimitedPartnerType destinationData = LimitedPartnerMapper.INSTANCE.mapPartnerTypeToEnum(sourceData);
        // then
        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void givenInvalidPartnerTypeString_whenMapsToEnum_thenIllegalArgumentException() {
        // given
        String invalidPartnerType = "Invalid Partner Type";
        // then
        assertThrows(IllegalArgumentException.class, () -> LimitedPartnerMapper.INSTANCE.mapPartnerTypeToEnum(invalidPartnerType));
    }

    @Test
    void givenValidPartnerTypeString_whenMapPartnerTypeToEnum_thenCorrect() {
        // given
        String partnerTypeString = "person";

        // when
        LimitedPartnerType result = mapper.mapPartnerTypeToEnum(partnerTypeString);

        // then
        assertEquals(LimitedPartnerType.PERSON, result);
    }

    @Test
    void givenNullPartnerTypeString_whenMapPartnerTypeToEnum_thenNull() {
        // given
        String partnerTypeString = null;

        // when
        LimitedPartnerType result = mapper.mapPartnerTypeToEnum(partnerTypeString);

        // then
        assertNull(result);
    }

    @Test
    void givenInvalidPartnerTypeString_whenMapPartnerTypeToEnum_thenException() {
        // given
        String partnerTypeString = "invalid";

        // when + then
        assertThrows(IllegalArgumentException.class, () -> mapper.mapPartnerTypeToEnum(partnerTypeString));
    }
}
