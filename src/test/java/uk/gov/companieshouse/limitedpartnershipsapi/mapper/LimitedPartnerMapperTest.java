package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;

public class LimitedPartnerMapperTest {

    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        final String eTag = "eTag";
        LimitedPartnerDao source = new LimitedPartnerDao();
        LimitedPartnerDataDao sourceData = new LimitedPartnerDataDao();
        sourceData.setKind(FILING_KIND_LIMITED_PARTNER);
        sourceData.setEtag(eTag);
        source.setData(sourceData);

        // when
        LimitedPartnerDto destination = LimitedPartnerMapper.INSTANCE.daoToDto(source);

        // then
        assertEquals(FILING_KIND_LIMITED_PARTNER, destination.getKind());
        assertEquals(eTag, destination.getEtag());
    }
}
