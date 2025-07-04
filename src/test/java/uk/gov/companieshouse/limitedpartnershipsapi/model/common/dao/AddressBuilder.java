package uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;

public class AddressBuilder {

    public static AddressBuilder addressBuilder() {
        return new AddressBuilder();
    }

    public AddressDao buildDao() {
        AddressDao addressDao = new AddressDao();
        addressDao.setPremises("33");
        addressDao.setAddressLine1("Acacia Avenue");
        addressDao.setLocality("Birmingham");
        addressDao.setCountry("England");
        addressDao.setPostalCode("BM1 2EH");
        return addressDao;
    }

    public AddressDto buildDto() {
        AddressDto addressDto = new AddressDto();
        addressDto.setPremises("33");
        addressDto.setAddressLine1("Acacia Avenue");
        addressDto.setLocality("Birmingham");
        addressDto.setCountry("England");
        addressDto.setPostalCode("BM1 2EH");
        return addressDto;
    }

}
