package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimitedPartnershipMapperTest {

    @InjectMocks
    LimitedPartnershipMapper mapper;

    @Test
    void givenDto_whenMapsToDao_thenCorrect() {
        // given
        LimitedPartnershipSubmissionDto source = new LimitedPartnershipSubmissionDto();
        DataDto sourceData = new DataDto();
        sourceData.setPartnershipName("Joe Bloggs");
        sourceData.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        sourceData.setPartnershipType(PartnershipType.LP);
        source.setData(sourceData);

        // when
        LimitedPartnershipSubmissionDao destination = LimitedPartnershipMapper.INSTANCE.dtoToDao(source);

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
        // then
        assertThrows(IllegalArgumentException.class, () -> LimitedPartnershipMapper.INSTANCE.mapPartnershipNameEndingToEnum(invalidNameEnding));
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
}
