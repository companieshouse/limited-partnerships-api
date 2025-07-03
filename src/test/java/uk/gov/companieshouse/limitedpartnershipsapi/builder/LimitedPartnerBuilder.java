package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.Currency;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes.SHARES;

public class LimitedPartnerBuilder {
    public static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";

    public LimitedPartnerDto personDto() {
        LimitedPartnerDto dto = new LimitedPartnerDto();

        dto.setId(LIMITED_PARTNER_ID);
        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setForename("Jack");
        dataDto.setSurname("Jones");
        dataDto.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDto.setNationality1(Nationality.EMIRATI);

        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDto.setContributionSubTypes(contributionSubTypes);
        dataDto.setUsualResidentialAddress(createAddressDto());
        dto.setData(dataDto);

        return dto;
    }

    public LimitedPartnerDto legalEntityDto() {
        LimitedPartnerDto dto = new LimitedPartnerDto();

        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setLegalEntityName("Legal Entity Name");
        dataDto.setLegalForm("Form");
        dataDto.setGoverningLaw("Act of law");
        dataDto.setLegalEntityRegisterName("Register of United States");
        dataDto.setLegalEntityRegistrationLocation(Country.UNITED_STATES);
        dataDto.setRegisteredCompanyNumber("12345678");
        dataDto.setContributionCurrencyType(Currency.GBP);
        dataDto.setContributionCurrencyValue("1000");
        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDto.setContributionSubTypes(contributionSubTypes);
        dataDto.setPrincipalOfficeAddress(createAddressDto());
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

    public LimitedPartnerDao personDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        dao.setId(LIMITED_PARTNER_ID);
        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setNationality1(Nationality.EMIRATI.getDescription());

        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDao.setContributionCurrencyValue("11.00");
        dataDao.setContributionCurrencyType(Currency.GBP);
        dataDao.setContributionSubTypes(contributionSubTypes);
        dataDao.setUsualResidentialAddress(createAddressDao());
        dao.setData(dataDao);

        return dao;
    }

    public LimitedPartnerDao legalEntityDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();

        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setContributionCurrencyType(Currency.GBP);
        dataDao.setRegisteredCompanyNumber("12345678");
        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDao.setContributionSubTypes(contributionSubTypes);
        dataDao.setPrincipalOfficeAddress(createAddressDao());

        dao.setData(dataDao);
        dao.setId(LIMITED_PARTNER_ID);

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
