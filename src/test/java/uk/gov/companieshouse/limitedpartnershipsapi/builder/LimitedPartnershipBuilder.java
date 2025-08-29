package uk.gov.companieshouse.limitedpartnershipsapi.builder;

import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

public class LimitedPartnershipBuilder {
    public static final String SUBMISSION_ID = "098aad0e-f45e-48aa-b320-dc4d3d76d0c0";

    private static final String PARTNERSHIP_NAME = "Test Partnership";
    private static final PartnershipNameEnding PARTNERSHIP_NAME_ENDING = PartnershipNameEnding.LIMITED_PARTNERSHIP;
    private static final PartnershipType PARTNERSHIP_TYPE = PartnershipType.LP;
    private static final String PARTNERSHIP_NUMBER = "LP123456";
    private Term term = Term.BY_AGREEMENT;
    private List<String> sicCodes = List.of("62012");
    private static final String EMAIL = "test@test.com";
    private static final Jurisdiction JURISDICTION = Jurisdiction.ENGLAND_AND_WALES;
    private boolean lawfulPurposeStatementChecked = true;
    private LocalDate dateOfUpdate = null;

    private String partnershipKind = FILING_KIND_LIMITED_PARTNERSHIP;

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

    public LimitedPartnershipBuilder withPartnershipKind(PartnershipKind partnershipKind) {
        this.partnershipKind = partnershipKind.getDescription();
        return this;
    }

    public LimitedPartnershipBuilder withDateOfUpdate(LocalDate dateOfUpdate) {
        this.dateOfUpdate = dateOfUpdate;
        return this;
    }

    public LimitedPartnershipDto buildDto() {
        LimitedPartnershipDto dto = new LimitedPartnershipDto();
        LimitedPartnershipDataDto limitedPartnershipDataDto = new LimitedPartnershipDataDto();

        limitedPartnershipDataDto.setKind(partnershipKind);
        limitedPartnershipDataDto.setPartnershipName(PARTNERSHIP_NAME);
        limitedPartnershipDataDto.setNameEnding(PARTNERSHIP_NAME_ENDING);
        limitedPartnershipDataDto.setPartnershipType(PARTNERSHIP_TYPE);
        limitedPartnershipDataDto.setPartnershipNumber(PARTNERSHIP_NUMBER);
        limitedPartnershipDataDto.setEmail(EMAIL);
        limitedPartnershipDataDto.setJurisdiction(JURISDICTION);
        limitedPartnershipDataDto.setTerm(term);
        limitedPartnershipDataDto.setSicCodes(sicCodes);
        limitedPartnershipDataDto.setLawfulPurposeStatementChecked(lawfulPurposeStatementChecked);
        limitedPartnershipDataDto.setRegisteredOfficeAddress(registeredOfficeAddressDto);
        limitedPartnershipDataDto.setPrincipalPlaceOfBusinessAddress(principalPalceOfBusinessAddressDto);
        limitedPartnershipDataDto.setDateOfUpdate(dateOfUpdate);

        dto.setData(limitedPartnershipDataDto);

        return dto;
    }

    public LimitedPartnershipDao buildDao() {
        LimitedPartnershipDao dao = new LimitedPartnershipDao();
        dao.setId(SUBMISSION_ID);

        LimitedPartnershipDataDao limitedPartnershipDataDao = new LimitedPartnershipDataDao();

        limitedPartnershipDataDao.setKind(partnershipKind);
        limitedPartnershipDataDao.setPartnershipName(PARTNERSHIP_NAME);
        limitedPartnershipDataDao.setNameEnding(PARTNERSHIP_NAME_ENDING.getDescription());
        limitedPartnershipDataDao.setPartnershipType(PARTNERSHIP_TYPE);
        limitedPartnershipDataDao.setPartnershipNumber(PARTNERSHIP_NUMBER);
        limitedPartnershipDataDao.setEmail(EMAIL);
        limitedPartnershipDataDao.setJurisdiction(JURISDICTION.getApiKey());
        limitedPartnershipDataDao.setTerm(term);
        limitedPartnershipDataDao.setSicCodes(sicCodes);
        limitedPartnershipDataDao.setLawfulPurposeStatementChecked(lawfulPurposeStatementChecked);
        limitedPartnershipDataDao.setRegisteredOfficeAddress(registeredOfficeAddressDao);
        limitedPartnershipDataDao.setPrincipalPlaceOfBusinessAddress(principalPalceOfBusinessAddressDao);
        limitedPartnershipDataDao.setDateOfUpdate(dateOfUpdate);

        dao.setData(limitedPartnershipDataDao);

        return dao;
    }

}
