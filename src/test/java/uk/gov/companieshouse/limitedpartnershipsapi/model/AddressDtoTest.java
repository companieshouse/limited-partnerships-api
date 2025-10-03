package uk.gov.companieshouse.limitedpartnershipsapi.model;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressDtoTest {

    @Test
    void testSetCountrySetsOverseasCorrectly() {
        AddressDto domestic = new AddressDto();
        domestic.setCountry("England");
        AddressDto domesticUk = new AddressDto();
        domesticUk.setCountry("United Kingdom");
        AddressDto overseas = new AddressDto();
        overseas.setCountry("France");
        AddressDto nullCountry = new AddressDto();
        nullCountry.setCountry(null);


        assertFalse(domestic.getOverseas());
        assertFalse(domesticUk.getOverseas());
        assertTrue(overseas.getOverseas());
        assertFalse(nullCountry.getOverseas());
    }

}
