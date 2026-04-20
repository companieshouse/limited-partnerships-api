package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;

public class AddressDtoBuilder {
    private final AddressDto address = new AddressDto();

    public AddressDtoBuilder withAddressLine1(String addressLine1) {
        address.setAddressLine1(addressLine1);
        return this;
    }

    public AddressDtoBuilder withAddressLine2(String addressLine2) {
        address.setAddressLine2(addressLine2);
        return this;
    }

    public AddressDtoBuilder withCountry(String country) {
        address.setCountry(country);
        return this;
    }

    public AddressDtoBuilder withLocality(String locality) {
        address.setLocality(locality);
        return this;
    }

    public AddressDtoBuilder withPremises(String premises) {
        address.setPremises(premises);
        return this;
    }

    public AddressDtoBuilder withRegion(String region) {
        address.setRegion(region);
        return this;
    }

    public AddressDtoBuilder withPostalCode(String postalCode) {
        address.setPostalCode(postalCode);
        return this;
    }

    public AddressDto build() {
        return address;
    }
}
