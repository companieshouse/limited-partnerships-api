package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LimitedPartnershipMapperTest {

    @Test
    void givenDto_whenMapsToDao_thenCorrect() {
        // given
        LimitedPartnershipDto source = new LimitedPartnershipDto();
        LimitedPartnershipDataDto sourceData = new LimitedPartnershipDataDto();
        sourceData.setPartnershipName("Joe Bloggs");
        sourceData.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        sourceData.setPartnershipType(PartnershipType.LP);
        source.setData(sourceData);

        // when
        LimitedPartnershipDao destination = LimitedPartnershipMapper.INSTANCE.dtoToDao(source);

        // then
        LimitedPartnershipDataDao destinationData = destination.getData();
        assertEquals(sourceData.getPartnershipName(), destinationData.getPartnershipName());
        assertEquals(sourceData.getNameEnding(), destinationData.getNameEnding());
        assertEquals(sourceData.getPartnershipType(), destinationData.getPartnershipType());
    }

    @Test
    void givenNameEndingEnum_whenMapsToString_thenCorrect() {
        // given
        PartnershipNameEnding sourceData = PartnershipNameEnding.LIMITED_PARTNERSHIP;
        // when
        String destinationData = LimitedPartnershipMapper.INSTANCE.mapPartnershipNameEndingToString(sourceData);
        // then
        assertEquals(sourceData.getDescription(), destinationData);
    }

    @Test
    void givenNameEndingString_whenMapsToEnum_thenCorrect() {
        // given
        String sourceData = PartnershipNameEnding.LIMITED_PARTNERSHIP.getDescription();
        // when
        PartnershipNameEnding destinationData = LimitedPartnershipMapper.INSTANCE.mapPartnershipNameEndingToEnum(sourceData);
        // then
        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void givenInvalidNameEndingString_whenMapsToEnum_thenIllegalArgumentException() {
        // given
        String invalidNameEnding = "Invalid Name Ending";
        // when
        PartnershipNameEnding destinationData = LimitedPartnershipMapper.INSTANCE.mapPartnershipNameEndingToEnum(invalidNameEnding);
        // then
        assertEquals(PartnershipNameEnding.UNKNOWN.getDescription(), destinationData.getDescription());
    }

    @Test
    void givenJurisdictionEnum_whenMapsToString_thenCorrect() {
        // given
        Jurisdiction sourceData = Jurisdiction.ENGLAND_AND_WALES;
        // when
        String destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToString(sourceData);
        // then
        assertEquals(sourceData.getApiKey(), destinationData);
    }

    @Test
    void givenJurisdictionString_whenMapsToEnum_thenCorrect() {
        // given
        String sourceData = Jurisdiction.NORTHERN_IRELAND.getApiKey();
        // when
        Jurisdiction destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToEnum(sourceData);
        // then
        assertEquals(sourceData, destinationData.getApiKey());
    }

    @Test
    void givenInvalidJurisdictionString_whenMapsToEnum_thenUnknownReturned() {
        // given
        String invalidJurisdiction = "Invalid Jurisdiction";
        // when
        Jurisdiction destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToEnum(invalidJurisdiction);
        // then
        assertEquals(Jurisdiction.UNKNOWN.getApiKey(), destinationData.getApiKey());
    }

    @Test
    void givenNullJurisdiction_whenMapsToEnum_thenNullReturned() {
        // when
        Jurisdiction destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToEnum(null);
        // then
        assertNull(destinationData);
    }
}
