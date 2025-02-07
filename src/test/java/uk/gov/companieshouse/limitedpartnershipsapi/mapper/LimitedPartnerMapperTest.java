package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LimitedPartnerMapperTest {

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
}
