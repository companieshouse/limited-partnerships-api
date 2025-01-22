package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService.LIMITED_PARTNERSHIP_REGISTRATION_KIND;

class LimitedPartnershipIncorporationMapperTest {

    @InjectMocks
    LimitedPartnershipIncorporationMapper mapper;

    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        LimitedPartnershipIncorporationDao source = new LimitedPartnershipIncorporationDao();
        IncorporationDataDao sourceData = new IncorporationDataDao();
        sourceData.setKind(LIMITED_PARTNERSHIP_REGISTRATION_KIND);
        source.setData(sourceData);

        // when
        LimitedPartnershipIncorporationDto destination = LimitedPartnershipIncorporationMapper.INSTANCE.daoToDto(source);

        // then
        assertEquals(LIMITED_PARTNERSHIP_REGISTRATION_KIND, destination.getKind());
    }
}
