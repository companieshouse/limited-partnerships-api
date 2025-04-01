package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LimitedPartnershipMapperTest {

    @Test
    void givenDto_whenMapsToDao_thenCorrect() {
        // given
        LimitedPartnershipDto source = new LimitedPartnershipDto();
        DataDto sourceData = new DataDto();
        sourceData.setPartnershipName("Joe Bloggs");
        sourceData.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        sourceData.setPartnershipType(PartnershipType.LP);
        source.setData(sourceData);

        // when
        LimitedPartnershipDao destination = LimitedPartnershipMapper.INSTANCE.dtoToDao(source);

        // then
        DataDao destinationData = destination.getData();
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
        assertEquals(sourceData.getDescription(), destinationData);
    }

    @Test
    void givenJurisdictionString_whenMapsToEnum_thenCorrect() {
        // given
        String sourceData = Jurisdiction.NORTHERN_IRELAND.getDescription();
        // when
        Jurisdiction destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToEnum(sourceData);
        // then
        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void givenInvalidJurisdictionString_whenMapsToEnum_thenUnknownReturned() {
        // given
        String invalidJurisdiction = "Invalid Jurisdiction";
        // when
        Jurisdiction destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToEnum(invalidJurisdiction);
        // then
        assertEquals(Jurisdiction.UNKNOWN.getDescription(), destinationData.getDescription());
    }

    @Test
    void givenNullJurisdiction_whenMapsToEnum_thenNullReturned() {
        // given
        String invalidJurisdiction = null;
        // when
        Jurisdiction destinationData = LimitedPartnershipMapper.INSTANCE.mapJurisdictionToEnum(invalidJurisdiction);
        // then
        assertNull(destinationData);
    }
}
