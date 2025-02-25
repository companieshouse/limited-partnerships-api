package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipIncorporationDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.Constants.FILING_KIND_REGISTRATION;

class LimitedPartnershipIncorporationMapperTest {

    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        final String eTag = "eTag";
        LimitedPartnershipIncorporationDao source = new LimitedPartnershipIncorporationDao();
        IncorporationDataDao sourceData = new IncorporationDataDao();
        sourceData.setKind(FILING_KIND_REGISTRATION);
        sourceData.setEtag(eTag);
        source.setData(sourceData);

        // when
        LimitedPartnershipIncorporationDto destination = LimitedPartnershipIncorporationMapper.INSTANCE.daoToDto(source);

        // then
        assertEquals(FILING_KIND_REGISTRATION, destination.getKind());
        assertEquals(eTag, destination.getEtag());
    }
}
