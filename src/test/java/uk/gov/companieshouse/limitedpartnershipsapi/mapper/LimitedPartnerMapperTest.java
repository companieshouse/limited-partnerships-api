package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LimitedPartnerMapperTest {

    @Autowired
    LimitedPartnershipMapper mapper;

    @Test
    public void givenDtoToDao_whenMaps_thenCorrect() {
        // given
        LimitedPartnershipSubmissionDto source = new LimitedPartnershipSubmissionDto();
        DataDto sourceData = new DataDto();
        sourceData.setPartnershipName("Joe Bloggs");
        sourceData.setNameEnding("Limited Partnership");
        source.setData(sourceData);

        // when
        LimitedPartnershipSubmissionDao destination = LimitedPartnershipMapper.INSTANCE.dtoToDao(source);

        // then
        DataDao destinationData = destination.getData();
        assertEquals(destinationData.getPartnershipName(), sourceData.getPartnershipName());
        assertEquals(destinationData.getNameEnding(), sourceData.getNameEnding());
    }
}
