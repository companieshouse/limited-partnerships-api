package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;

import java.time.LocalDate;

public class GeneralPartnerBuilder {
    public static final String GENERAL_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";

    public GeneralPartnerDto dto() {
        GeneralPartnerDto dto = new GeneralPartnerDto();

        dto.setId(GENERAL_PARTNER_ID);
        GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
        dataDto.setForename("Jack");
        dataDto.setSurname("Jones");
        dataDto.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDto.setNationality1(Nationality.BRITISH);
        dataDto.setNotDisqualifiedStatementChecked(true);
        dataDto.setUsualResidentialAddress(createAddressDto());
        dataDto.setServiceAddress(createAddressDto());
        dto.setData(dataDto);

        return dto;
    }

    private AddressDto createAddressDto() {
        AddressDto dto = new AddressDto();

        dto.setPremises("33");
        dto.setAddressLine1("Acacia Avenue");
        dto.setLocality("Birmingham");
        dto.setCountry("England");
        dto.setPostalCode("BM1 2EH");

        return dto;
    }

    public GeneralPartnerDao dao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();

        dao.setId(GENERAL_PARTNER_ID);
        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setNationality1(Nationality.BRITISH.getDescription());
        dataDao.setNotDisqualifiedStatementChecked(true);
        dataDao.setUsualResidentialAddress(createAddressDao());
        dataDao.setServiceAddress(createAddressDao());
        dao.setData(dataDao);

        return dao;
    }

    private AddressDao createAddressDao() {
        AddressDao dao = new AddressDao();

        dao.setPremises("33");
        dao.setAddressLine1("Acacia Avenue");
        dao.setLocality("Birmingham");
        dao.setCountry("England");
        dao.setPostalCode("BM1 2EH");

        return dao;
    }
}
