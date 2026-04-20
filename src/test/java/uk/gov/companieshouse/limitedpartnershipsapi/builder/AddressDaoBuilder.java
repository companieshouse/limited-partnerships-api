package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;

public class AddressDaoBuilder {
    private final AddressDao address = new AddressDao();

    public AddressDaoBuilder withAddressLine1(String addressLine1) {
        address.setAddressLine1(addressLine1);
        return this;
    }
    public AddressDaoBuilder withAddressLine2(String addressLine2) {
        address.setAddressLine2(addressLine2);
        return this;
    }
    public AddressDaoBuilder withCountry(String country) {
        address.setCountry(country);
        return this;
    }
    public AddressDaoBuilder withLocality(String locality) {
        address.setLocality(locality);
        return this;
    }
    public AddressDaoBuilder withPostalCode(String postalCode) {
        address.setPostalCode(postalCode);
        return this;
    }
    public AddressDaoBuilder withPremises(String premises) {
        address.setPremises(premises);
        return this;
    }
    public AddressDaoBuilder withRegion(String region) {
        address.setRegion(region);
        return this;
    }
    public AddressDao build() {
        return address;
    }
}
