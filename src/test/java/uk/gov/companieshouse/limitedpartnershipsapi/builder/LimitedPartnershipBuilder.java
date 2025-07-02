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

    private static final String PARTNERSHIP_NAME = "Asset Adders";
    private static final PartnershipNameEnding PARTNERSHIP_NAME_ENDING = PartnershipNameEnding.LIMITED_PARTNERSHIP;
    private static final PartnershipType PARTNERSHIP_TYPE = PartnershipType.LP;
    private static final String PARTNERSHIP_NUMBER = "LP123456";
    private Term term = Term.BY_AGREEMENT;
    private List<String> sicCodes = List.of("62012");
    private static final String EMAIL = "test@test.com";
    private static final Jurisdiction JURISDICTION = Jurisdiction.ENGLAND_AND_WALES;
    private boolean lawfulPurposeStatementChecked = true;
    private AddressDto registeredOfficeAddressDto = null;
    private AddressDto principalPalceOfBusinessAddressDto = null;
    private AddressDao registeredOfficeAddressDao = null;
    private AddressDao principalPalceOfBusinessAddressDao = null;

    private static final String PREMISES = "33";
    private static final String ADDRESS_LINE_1 = "Acacia Avenue";
    private static final String LOCALITY = "Birmingham";
    private static final Country COUNTRY = Country.ENGLAND;
    private static final String POSTAL_CODE = "BM1 2EH";

    private void createAddressDto() {
        AddressDto roaDto = new AddressDto();
        AddressDto ppobaDto = new AddressDto();

        roaDto.setPremises(PREMISES);
        roaDto.setAddressLine1(ADDRESS_LINE_1);
        roaDto.setLocality(LOCALITY);
        roaDto.setCountry(COUNTRY.getDescription());
        roaDto.setPostalCode(POSTAL_CODE);

        ppobaDto.setPremises(PREMISES);
        ppobaDto.setAddressLine1(ADDRESS_LINE_1);
        ppobaDto.setLocality(LOCALITY);
        ppobaDto.setCountry(COUNTRY.getDescription());
        ppobaDto.setPostalCode(POSTAL_CODE);

        this.registeredOfficeAddressDto = roaDto;
        this.principalPalceOfBusinessAddressDto = ppobaDto;
    }

    private void createAddressDao() {
        AddressDao roaDao = new AddressDao();
        AddressDao ppobaDao = new AddressDao();

        roaDao.setPremises(PREMISES);
        roaDao.setAddressLine1(ADDRESS_LINE_1);
        roaDao.setLocality(LOCALITY);
        roaDao.setCountry(COUNTRY.getDescription());
        roaDao.setPostalCode(POSTAL_CODE);

        ppobaDao.setPremises(PREMISES);
        ppobaDao.setAddressLine1(ADDRESS_LINE_1);
        ppobaDao.setLocality(LOCALITY);
        ppobaDao.setCountry(COUNTRY.getDescription());
        ppobaDao.setPostalCode(POSTAL_CODE);

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

        roaDao.setPremises(PREMISES);
        roaDao.setAddressLine1(ADDRESS_LINE_1);
        roaDao.setLocality(LOCALITY);
        roaDao.setCountry(COUNTRY.getDescription());
        roaDao.setPostalCode(POSTAL_CODE);

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

        ppobaDao.setPremises(PREMISES);
        ppobaDao.setAddressLine1(ADDRESS_LINE_1);
        ppobaDao.setLocality(LOCALITY);
        ppobaDao.setCountry(COUNTRY.getDescription());
        ppobaDao.setPostalCode(POSTAL_CODE);

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

        dataDto.setPartnershipName(PARTNERSHIP_NAME);
        dataDto.setNameEnding(PARTNERSHIP_NAME_ENDING);
        dataDto.setPartnershipType(PARTNERSHIP_TYPE);
        dataDto.setPartnershipNumber(PARTNERSHIP_NUMBER);
        dataDto.setEmail(EMAIL);
        dataDto.setJurisdiction(JURISDICTION);
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

        dataDao.setPartnershipName(PARTNERSHIP_NAME);
        dataDao.setNameEnding(PARTNERSHIP_NAME_ENDING.getDescription());
        dataDao.setPartnershipType(PARTNERSHIP_TYPE);
        dataDao.setPartnershipNumber(PARTNERSHIP_NUMBER);
        dataDao.setEmail(EMAIL);
        dataDao.setJurisdiction(JURISDICTION.getApiKey());
        dataDao.setTerm(term);
        dataDao.setSicCodes(sicCodes);
        dataDao.setLawfulPurposeStatementChecked(lawfulPurposeStatementChecked);
        dataDao.setRegisteredOfficeAddress(registeredOfficeAddressDao);
        dataDao.setPrincipalPlaceOfBusinessAddress(principalPalceOfBusinessAddressDao);

        dao.setData(dataDao);

        return dao;
    }
}
