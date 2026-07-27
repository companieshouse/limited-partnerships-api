package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PartnershipMapperTest {

    @Test
    void givenDto_whenMapsToDao_thenCorrect() {
        // given
        PartnershipDto source = new PartnershipDto();
        DataDto sourceData = new DataDto();
        sourceData.setPartnershipName("Joe Bloggs");
        sourceData.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        sourceData.setPartnershipType(PartnershipType.LP);
        source.setData(sourceData);

        // when
        PartnershipDao destination = PartnershipMapper.INSTANCE.dtoToDao(source);

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
        String destinationData = PartnershipMapper.INSTANCE.mapPartnershipNameEndingToString(sourceData);
        // then
        assertEquals(sourceData.getDescription(), destinationData);
    }

    @Test
    void givenNameEndingString_whenMapsToEnum_thenCorrect() {
        // given
        String sourceData = PartnershipNameEnding.LIMITED_PARTNERSHIP.getDescription();
        // when
        PartnershipNameEnding destinationData = PartnershipMapper.INSTANCE.mapPartnershipNameEndingToEnum(sourceData);
        // then
        assertEquals(sourceData, destinationData.getDescription());
    }

    @Test
    void givenInvalidNameEndingString_whenMapsToEnum_thenIllegalArgumentException() {
        // given
        String invalidNameEnding = "Invalid Name Ending";
        // when
        PartnershipNameEnding destinationData = PartnershipMapper.INSTANCE.mapPartnershipNameEndingToEnum(invalidNameEnding);
        // then
        assertEquals(PartnershipNameEnding.UNKNOWN.getDescription(), destinationData.getDescription());
    }

    @Test
    void givenJurisdictionEnum_whenMapsToString_thenCorrect() {
        // given
        Jurisdiction sourceData = Jurisdiction.ENGLAND_AND_WALES;
        // when
        String destinationData = PartnershipMapper.INSTANCE.mapJurisdictionToString(sourceData);
        // then
        assertEquals(sourceData.getApiKey(), destinationData);
    }

    @Test
    void givenJurisdictionString_whenMapsToEnum_thenCorrect() {
        // given
        String sourceData = Jurisdiction.NORTHERN_IRELAND.getApiKey();
        // when
        Jurisdiction destinationData = PartnershipMapper.INSTANCE.mapJurisdictionToEnum(sourceData);
        // then
        assertEquals(sourceData, destinationData.getApiKey());
    }

    @Test
    void givenInvalidJurisdictionString_whenMapsToEnum_thenUnknownReturned() {
        // given
        String invalidJurisdiction = "Invalid Jurisdiction";
        // when
        Jurisdiction destinationData = PartnershipMapper.INSTANCE.mapJurisdictionToEnum(invalidJurisdiction);
        // then
        assertEquals(Jurisdiction.UNKNOWN.getApiKey(), destinationData.getApiKey());
    }

    @Test
    void givenNullJurisdiction_whenMapsToEnum_thenNullReturned() {
        // when
        Jurisdiction destinationData = PartnershipMapper.INSTANCE.mapJurisdictionToEnum(null);
        // then
        assertNull(destinationData);
    }
}
