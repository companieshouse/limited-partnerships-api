package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressUtilsTest {

    @Test
    void setOverseasAddressIndicators_DataDto_setsIndicatorCorrectly() {
        AddressDto domestic = new AddressDto();
        domestic.setCountry("England");
        AddressDto overseas = new AddressDto();
        overseas.setCountry("France");

        DataDto dto = new DataDto();
        dto.setRegisteredOfficeAddress(domestic);
        dto.setPrincipalPlaceOfBusinessAddress(overseas);

        AddressUtils.setOverseasAddressIndicators(dto);

        assertFalse(domestic.getOverseas());
        assertTrue(overseas.getOverseas());
    }

    @Test
    void setOverseasAddressIndicators_GeneralPartnerDataDto_setsIndicatorCorrectly() {
        AddressDto domestic = new AddressDto();
        domestic.setCountry("Wales");
        AddressDto overseas = new AddressDto();
        overseas.setCountry("Germany");

        GeneralPartnerDataDto dto = new GeneralPartnerDataDto();
        dto.setServiceAddress(domestic);
        dto.setPrincipalOfficeAddress(overseas);

        AddressUtils.setOverseasAddressIndicators(dto);

        assertFalse(domestic.getOverseas());
        assertTrue(overseas.getOverseas());
    }

    @Test
    void setOverseasAddressIndicators_LimitedPartnerDataDto_setsIndicatorCorrectly() {
        AddressDto domestic = new AddressDto();
        domestic.setCountry("United Kingdom");
        AddressDto overseas = new AddressDto();
        overseas.setCountry("Spain");

        LimitedPartnerDataDto dto = new LimitedPartnerDataDto();
        dto.setPrincipalOfficeAddress(domestic);
        dto.setUsualResidentialAddress(overseas);

        AddressUtils.setOverseasAddressIndicators(dto);

        assertFalse(domestic.getOverseas());
        assertTrue(overseas.getOverseas());
    }

    @Test
    void setOverseasAddressIndicators_handlesNullInput() {
        // Should not throw
        assertDoesNotThrow(() -> AddressUtils.setOverseasAddressIndicators((DataDto) null));
        assertDoesNotThrow(() -> AddressUtils.setOverseasAddressIndicators((GeneralPartnerDataDto) null));
        assertDoesNotThrow(() -> AddressUtils.setOverseasAddressIndicators((LimitedPartnerDataDto) null));
    }
}
