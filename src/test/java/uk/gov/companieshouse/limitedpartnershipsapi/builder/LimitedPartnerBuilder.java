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
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.ContributionSubTypes.SHARES;

public class LimitedPartnerBuilder {
    public static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";

    private String limitedPartnerKind = null;
    private LocalDate ceaseDate = null;
    private boolean removeConfirmationChecked;
    private PartnershipType partnershipType;

    private Boolean updateUsualResidentialAddressRequired;
    private Boolean updatePrincipalOfficeAddressRequired;

    private String forename = "Jack";
    private String surname = "Jones";
    private Nationality nationality1 = Nationality.EMIRATI;
    private Nationality nationality2 = null;
    private String legalEntityName = "Legal Entity Name";
    private String legalForm = "Form";
    private String governingLaw = "Act of law";
    private String legalEntityRegisterName = "Register of United States";
    private Country legalEntityRegistrationLocation = Country.UNITED_STATES;
    private String registeredCompanyNumber = "12345678";

    public LimitedPartnerBuilder withLimitedPartnerKind(String kind) {
        this.limitedPartnerKind = kind;
        return this;
    }

    public LimitedPartnerBuilder withCeaseDate(LocalDate ceaseDate) {
        this.ceaseDate = ceaseDate;
        return this;
    }

    public LimitedPartnerBuilder withRemoveConfirmationChecked(boolean removeConfirmationChecked) {
        this.removeConfirmationChecked = removeConfirmationChecked;
        return this;
    }

    public LimitedPartnerBuilder withPartnershipType(PartnershipType partnershipType) {
        this.partnershipType = partnershipType;
        return this;
    }

    public LimitedPartnerBuilder withUpdateUsualResidentialAddressRequired(Boolean updateUsualResidentialAddressRequired) {
        this.updateUsualResidentialAddressRequired = updateUsualResidentialAddressRequired;
        return this;
    }

    public LimitedPartnerBuilder withUpdatePrincipalOfficeAddressRequired(Boolean updatePrincipalOfficeAddressRequired) {
        this.updatePrincipalOfficeAddressRequired = updatePrincipalOfficeAddressRequired;
        return this;
    }

    public LimitedPartnerBuilder withForename(String forename) {
        this.forename = forename;
        return this;
    }

    public LimitedPartnerBuilder withSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public LimitedPartnerBuilder withNationality1(Nationality nationality1) {
        this.nationality1 = nationality1;
        return this;
    }

    public LimitedPartnerBuilder withNationality2(Nationality nationality2) {
        this.nationality2 = nationality2;
        return this;
    }

    public LimitedPartnerBuilder withLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
        return this;
    }

    public LimitedPartnerBuilder withLegalForm(String legalForm) {
        this.legalForm = legalForm;
        return this;
    }

    public LimitedPartnerBuilder withGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
        return this;
    }

    public LimitedPartnerBuilder withLegalEntityRegisterName(String legalEntityRegisterName) {
        this.legalEntityRegisterName = legalEntityRegisterName;
        return this;
    }

    public LimitedPartnerBuilder withLegalEntityRegistrationLocation(Country legalEntityRegistrationLocation) {
        this.legalEntityRegistrationLocation = legalEntityRegistrationLocation;
        return this;
    }

    public LimitedPartnerBuilder withRegisteredCompanyNumber(String registeredCompanyNumber) {
        this.registeredCompanyNumber = registeredCompanyNumber;
        return this;
    }


    public LimitedPartnerDto personDto() {
        LimitedPartnerDto dto = new LimitedPartnerDto();
        dto.setId(LIMITED_PARTNER_ID);

        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setForename(forename);
        dataDto.setSurname(surname);
        dataDto.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDto.setNationality1(nationality1);
        dataDto.setNationality2(nationality2);
        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDto.setContributionSubTypes(contributionSubTypes);
        dataDto.setContributionCurrencyValue("1000.00");
        dataDto.setContributionCurrencyType(Currency.GBP);
        dataDto.setUsualResidentialAddress(createAddressDto());
        dataDto.setServiceAddress(createAddressDto());
        dataDto.setUpdateUsualResidentialAddressRequired(updateUsualResidentialAddressRequired);
        dataDto.setKind(limitedPartnerKind);
        dataDto.setCeaseDate(ceaseDate);
        dataDto.setRemoveConfirmationChecked(removeConfirmationChecked);
        dataDto.setPartnershipType(partnershipType);

        dto.setData(dataDto);

        return dto;
    }

    public LimitedPartnerDto legalEntityDto() {
        LimitedPartnerDto dto = new LimitedPartnerDto();
        dto.setId(LIMITED_PARTNER_ID);

        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setLegalEntityName(legalEntityName);
        dataDto.setLegalForm(legalForm);
        dataDto.setGoverningLaw(governingLaw);
        dataDto.setLegalEntityRegisterName(legalEntityRegisterName);
        dataDto.setLegalEntityRegistrationLocation(legalEntityRegistrationLocation);
        dataDto.setRegisteredCompanyNumber(registeredCompanyNumber);
        dataDto.setContributionCurrencyType(Currency.GBP);
        dataDto.setContributionCurrencyValue("1000.00");

        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDto.setContributionSubTypes(contributionSubTypes);
        dataDto.setPrincipalOfficeAddress(createAddressDto());
        dataDto.setUpdatePrincipalOfficeAddressRequired(updatePrincipalOfficeAddressRequired);
        dataDto.setKind(limitedPartnerKind);
        dataDto.setCeaseDate(ceaseDate);
        dataDto.setRemoveConfirmationChecked(removeConfirmationChecked);
        dataDto.setPartnershipType(partnershipType);

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
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setForename("Jack");
        dataDao.setSurname("Jones");
        dataDao.setDateOfBirth(LocalDate.of(2000, 10, 3));
        dataDao.setNationality1(Nationality.EMIRATI.getDescription());

        List<ContributionSubTypes> contributionSubTypes = new ArrayList<>();
        contributionSubTypes.add(SHARES);
        dataDao.setContributionCurrencyValue("1000.00");
        dataDao.setContributionCurrencyType(Currency.GBP);
        dataDao.setContributionSubTypes(contributionSubTypes);
        dataDao.setUsualResidentialAddress(createAddressDao());
        dataDao.setServiceAddress(createAddressDao());
        dataDao.setUpdateUsualResidentialAddressRequired(updateUsualResidentialAddressRequired);
        dataDao.setKind(limitedPartnerKind);

        dao.setData(dataDao);

        return dao;
    }

    public LimitedPartnerDao legalEntityDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();
        dao.setId(LIMITED_PARTNER_ID);
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

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
        dataDao.setContributionCurrencyValue("1000.00");
        dataDao.setContributionCurrencyType(Currency.GBP);
        dataDao.setPrincipalOfficeAddress(createAddressDao());
        dataDao.setUpdatePrincipalOfficeAddressRequired(updatePrincipalOfficeAddressRequired);
        dataDao.setKind(limitedPartnerKind);

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
