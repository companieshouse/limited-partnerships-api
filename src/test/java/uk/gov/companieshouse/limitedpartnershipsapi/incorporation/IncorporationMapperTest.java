package uk.gov.companieshouse.limitedpartnershipsapi.incorporation;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dao.IncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.dto.LimitedPartnershipIncorporationDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode.REGISTRATION;

class IncorporationMapperTest {

    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        final String eTag = "eTag";
        IncorporationDao source = new IncorporationDao();
        IncorporationDataDao sourceData = new IncorporationDataDao();
        sourceData.setKind(REGISTRATION.getDescription());
        sourceData.setEtag(eTag);
        source.setData(sourceData);

        // when
        LimitedPartnershipIncorporationDto destination = IncorporationMapper.INSTANCE.daoToDto(source);

        // then
        assertEquals(REGISTRATION.getDescription(), destination.getKind());
        assertEquals(eTag, destination.getEtag());
    }
}
