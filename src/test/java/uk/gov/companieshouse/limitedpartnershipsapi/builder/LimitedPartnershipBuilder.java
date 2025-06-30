package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.util.List;

public class LimitedPartnershipBuilder {
    public static final String SUBMISSION_ID = "098aad0e-f45e-48aa-b320-dc4d3d76d0c0";

    private static final String partnershipName = "Asset Adders";
    private static final PartnershipNameEnding partnershipNameEnding = PartnershipNameEnding.LIMITED_PARTNERSHIP;
    private static final PartnershipType partnershipType = PartnershipType.LP;
    private static final String partnershipNumber = "LP123456";
    private Term term = Term.BY_AGREEMENT;
    private List<String> sicCodes = List.of("62012");
    private static final String email = "test@test.com";
    private static final Jurisdiction jurisdiction = Jurisdiction.ENGLAND_AND_WALES;
    private boolean lawfulPurposeStatementChecked = true;
    private AddressDto registeredOfficeAddressDto = null;
    private AddressDto principalPalceOfBusinessAddressDto = null;
    private AddressDao registeredOfficeAddressDao = null;
    private AddressDao principalPalceOfBusinessAddressDao = null;

    private static final String premises = "33";
    private static final String addressLine1 = "Acacia Avenue";
    private static final String locality = "Birmingham";
    private static final Country country = Country.ENGLAND;
    private static final String postalCode = "BM1 2EH";

    private void createAddressDto() {
        AddressDto roaDto = new AddressDto();
        AddressDto ppobaDto = new AddressDto();

        roaDto.setPremises(premises);
        roaDto.setAddressLine1(addressLine1);
        roaDto.setLocality(locality);
        roaDto.setCountry(country.getDescription());
        roaDto.setPostalCode(postalCode);

        ppobaDto.setPremises(premises);
        ppobaDto.setAddressLine1(addressLine1);
        ppobaDto.setLocality(locality);
        ppobaDto.setCountry(country.getDescription());
        ppobaDto.setPostalCode(postalCode);

        this.registeredOfficeAddressDto = roaDto;
        this.principalPalceOfBusinessAddressDto = ppobaDto;
    }

    private void createAddressDao() {
        AddressDao roaDao = new AddressDao();
        AddressDao ppobaDao = new AddressDao();

        roaDao.setPremises(premises);
        roaDao.setAddressLine1(addressLine1);
        roaDao.setLocality(locality);
        roaDao.setCountry(country.getDescription());
        roaDao.setPostalCode(postalCode);

        ppobaDao.setPremises(premises);
        ppobaDao.setAddressLine1(addressLine1);
        ppobaDao.setLocality(locality);
        ppobaDao.setCountry(country.getDescription());
        ppobaDao.setPostalCode(postalCode);

        this.registeredOfficeAddressDao = roaDao;
        this.principalPalceOfBusinessAddressDao = ppobaDao;
    }

    public LimitedPartnershipBuilder withAddresses() {
        createAddressDto();
        createAddressDao();
        return this;
    }

    public LimitedPartnershipBuilder withRegisteredOfficeAddress(AddressDto addressDto) {

        if (addressDto == null) {
            this.registeredOfficeAddressDto = null;
            this.registeredOfficeAddressDao = null;

            return this;
        }

        AddressDao roaDao = new AddressDao();

        roaDao.setPremises(premises);
        roaDao.setAddressLine1(addressLine1);
        roaDao.setLocality(locality);
        roaDao.setCountry(country.getDescription());
        roaDao.setPostalCode(postalCode);

        this.registeredOfficeAddressDto = addressDto;
        this.registeredOfficeAddressDao = roaDao;

        return this;
    }

    public LimitedPartnershipBuilder withPrincipalPlaceOfBusinessAddress(AddressDto addressDto) {

        if (addressDto == null) {
            this.principalPalceOfBusinessAddressDto = null;
            this.principalPalceOfBusinessAddressDao = null;

            return this;
        }

        AddressDao ppobaDao = new AddressDao();

        ppobaDao.setPremises(premises);
        ppobaDao.setAddressLine1(addressLine1);
        ppobaDao.setLocality(locality);
        ppobaDao.setCountry(country.getDescription());
        ppobaDao.setPostalCode(postalCode);

        this.principalPalceOfBusinessAddressDto = addressDto;
        this.principalPalceOfBusinessAddressDao = ppobaDao;

        return this;
    }

    public LimitedPartnershipBuilder withTerm(Term term) {
        this.term = term;
        return this;
    }

    public LimitedPartnershipBuilder withSicCodes(List<String> sicCodes) {
        this.sicCodes = sicCodes;
        return this;
    }

    public LimitedPartnershipBuilder withLawfulPurposeStatementChecked(boolean lawfulPurposeStatementChecked) {
        this.lawfulPurposeStatementChecked = lawfulPurposeStatementChecked;
        return this;
    }

    public LimitedPartnershipDto buildDto() {
        LimitedPartnershipDto dto = new LimitedPartnershipDto();
        DataDto dataDto = new DataDto();

        dataDto.setPartnershipName(partnershipName);
        dataDto.setNameEnding(partnershipNameEnding);
        dataDto.setPartnershipType(partnershipType);
        dataDto.setPartnershipNumber(partnershipNumber);
        dataDto.setEmail(email);
        dataDto.setJurisdiction(jurisdiction);
        dataDto.setTerm(term);
        dataDto.setSicCodes(sicCodes);
        dataDto.setLawfulPurposeStatementChecked(lawfulPurposeStatementChecked);
        dataDto.setRegisteredOfficeAddress(registeredOfficeAddressDto);
        dataDto.setPrincipalPlaceOfBusinessAddress(principalPalceOfBusinessAddressDto);

        dto.setData(dataDto);

        return dto;
    }

    public LimitedPartnershipDao buildDao() {
        LimitedPartnershipDao dao = new LimitedPartnershipDao();
        dao.setId(SUBMISSION_ID);

        DataDao dataDao = new DataDao();

        dataDao.setPartnershipName(partnershipName);
        dataDao.setNameEnding(partnershipNameEnding.getDescription());
        dataDao.setPartnershipType(partnershipType);
        dataDao.setPartnershipNumber(partnershipNumber);
        dataDao.setEmail(email);
        dataDao.setJurisdiction(jurisdiction.getApiKey());
        dataDao.setTerm(term);
        dataDao.setSicCodes(sicCodes);
        dataDao.setLawfulPurposeStatementChecked(lawfulPurposeStatementChecked);
        dataDao.setRegisteredOfficeAddress(registeredOfficeAddressDao);
        dataDao.setPrincipalPlaceOfBusinessAddress(principalPalceOfBusinessAddressDao);

        dao.setData(dataDao);

        return dao;
    }
}
