package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dto.LimitedPartnershipIncorporationDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.IncorporationKind.REGISTRATION;

class LimitedPartnershipIncorporationMapperTest {

    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        final String eTag = "eTag";
        LimitedPartnershipIncorporationDao source = new LimitedPartnershipIncorporationDao();
        IncorporationDataDao sourceData = new IncorporationDataDao();
        sourceData.setKind(REGISTRATION.getDescription());
        sourceData.setEtag(eTag);
        source.setData(sourceData);

        // when
        LimitedPartnershipIncorporationDto destination = LimitedPartnershipIncorporationMapper.INSTANCE.daoToDto(source);

        // then
        assertEquals(REGISTRATION.getDescription(), destination.getKind());
        assertEquals(eTag, destination.getEtag());
    }
}
