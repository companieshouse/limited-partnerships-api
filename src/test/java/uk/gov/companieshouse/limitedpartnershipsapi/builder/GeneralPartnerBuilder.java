package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;

import java.time.LocalDate;

public class GeneralPartnerBuilder {
    public static final String GENERAL_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private String generalPartnerKind = null;
    private PartnershipType partnershipType;
    private LocalDate ceaseDate;

    private Boolean updateUsualResidentialAddressRequired;
    private Boolean updateServiceAddressRequired;
    private Boolean updatePrincipalOfficeAddressRequired;

    public GeneralPartnerBuilder withGeneralPartnerKind(String kind) {
        this.generalPartnerKind = kind;
        return this;
    }

    public GeneralPartnerBuilder withPartnershipType(PartnershipType partnershipType) {
        this.partnershipType = partnershipType;
        return this;
    }

    public GeneralPartnerBuilder withUpdateUsualResidentialAddressRequired(Boolean updateUsualResidentialAddressRequired) {
        this.updateUsualResidentialAddressRequired = updateUsualResidentialAddressRequired;
        return this;
    }

    public GeneralPartnerBuilder withUpdateServiceAddressRequired(Boolean updateServiceAddressRequired) {
        this.updateServiceAddressRequired = updateServiceAddressRequired;
        return this;
    }

    public GeneralPartnerBuilder withUpdatePrincipalOfficeAddressRequired(Boolean updatePrincipalOfficeAddressRequired) {
        this.updatePrincipalOfficeAddressRequired = updatePrincipalOfficeAddressRequired;
        return this;
    }

    public GeneralPartnerBuilder withCeaseDate(LocalDate ceaseDate) {
        this.ceaseDate = ceaseDate;
        return this;
    }

    public GeneralPartnerDto personDto() {
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
        dataDto.setDateEffectiveFrom(LocalDate.of(2024, 1, 1));
        dataDto.setKind(generalPartnerKind);
        dataDto.setPartnershipType(partnershipType);
        dataDto.setUpdateUsualResidentialAddressRequired(updateUsualResidentialAddressRequired);
        dataDto.setUpdateServiceAddressRequired(updateServiceAddressRequired);
        dataDto.setCeaseDate(ceaseDate);

        dto.setData(dataDto);

        return dto;
    }

    public GeneralPartnerDto legalEntityDto() {
        GeneralPartnerDto dto = new GeneralPartnerDto();
        dto.setId(GENERAL_PARTNER_ID);

        GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
        dataDto.setLegalEntityName("Legal Entity Name");
        dataDto.setLegalForm("Form");
        dataDto.setGoverningLaw("Act of law");
        dataDto.setLegalEntityRegisterName("Register of United States");
        dataDto.setLegalEntityRegistrationLocation(Country.UNITED_STATES);
        dataDto.setRegisteredCompanyNumber("12345678");
        dataDto.setPrincipalOfficeAddress(createAddressDto());
        dataDto.setDateEffectiveFrom(LocalDate.of(2024, 1, 1));
        dataDto.setNotDisqualifiedStatementChecked(true);
        dataDto.setKind(generalPartnerKind);
        dataDto.setPartnershipType(partnershipType);
        dataDto.setUpdatePrincipalOfficeAddressRequired(updatePrincipalOfficeAddressRequired);
        dataDto.setCeaseDate(ceaseDate);

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

    public GeneralPartnerDao personDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();
        dao.setId(GENERAL_PARTNER_ID);
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setNationality1(Nationality.BRITISH.getDescription());
        dataDao.setNotDisqualifiedStatementChecked(true);
        dataDao.setUsualResidentialAddress(createAddressDao());
        dataDao.setServiceAddress(createAddressDao());
        dataDao.setKind(generalPartnerKind);
        dataDao.setUpdateUsualResidentialAddressRequired(updateUsualResidentialAddressRequired);
        dataDao.setUpdateServiceAddressRequired(updateServiceAddressRequired);
        dataDao.setCeaseDate(ceaseDate);

        dao.setData(dataDao);

        return dao;
    }

    public GeneralPartnerDao legalEntityDao() {
        GeneralPartnerDao dao = new GeneralPartnerDao();
        dao.setId(GENERAL_PARTNER_ID);
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

        GeneralPartnerDataDao dataDao = new GeneralPartnerDataDao();
        dataDao.setLegalEntityName("My company ltd");
        dataDao.setLegalForm("Limited Company");
        dataDao.setGoverningLaw("Act of law");
        dataDao.setLegalEntityRegisterName("UK Register");
        dataDao.setLegalEntityRegistrationLocation("United Kingdom");
        dataDao.setRegisteredCompanyNumber("12345678");
        dataDao.setNotDisqualifiedStatementChecked(true);
        dataDao.setPrincipalOfficeAddress(createAddressDao());
        dataDao.setUpdatePrincipalOfficeAddressRequired(updatePrincipalOfficeAddressRequired);
        dataDao.setKind(generalPartnerKind);
        dataDao.setCeaseDate(ceaseDate);

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
