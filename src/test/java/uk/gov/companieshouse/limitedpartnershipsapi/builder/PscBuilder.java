package uk.gov.companieshouse.limitedpartnershipsapi.builder;


import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country.ENGLAND;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country.FRANCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.BRITISH;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.FRENCH;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.SPANISH;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PSC;

public class PscBuilder {
    public static final String ID = "1234";
    public static final String ETAG = "eTag";
    public static final String APPOINTMENT_ID = "1234";
    public static final String FORENAME = "John";
    public static final String FORMER_NAMES = "Doe";
    public static final String GOVERNING_LAW = "law of england";
    public static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    public static final String LEGAL_ENTITY_REGISTER_NAME = "Legal Entity Register Name";
    public static final String LEGAL_ENTITY_REGISTRATION_LOCATION = "England";
    public static final String LEGAL_FORM = "Legal Form";
    public static final String NATIONALITY1 = "British";
    public static final String NATIONALITY2 = "French";
    public static final String REGISTERED_COMPANY_NUMBER = "12345678";
    public static final boolean LEGAL_PERSONALITY_STATEMENT_CHECKED = true;
    public static final String SURNAME = "Smith";
    public static final String NATURE_OF_CONTROL = "test";
    public static final String POA_PREFIX = "poa";
    public static final String SERVICE_PREFIX = "service";
    public static final String URA_PREFIX = "ura";
    public static final String ADDRESS_LINE1_SUFFIX = " line1";
    public static final String ADDRESS_LINE2_SUFFIX = " line2";
    public static final String COUNTRY_SUFFIX = " England";
    public static final String LOCALITY_SUFFIX = " Birmingham";
    public static final String POSTAL_CODE_SUFFIX = " BM1 2EH";
    public static final String PREMISES_SUFFIX = " 22";
    public static final String REGION_SUFFIX = " West Midlands";
    public static final LocalDate DATE_EFFECTIVE_FROM = LocalDate.of(2026, 1, 20);
    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1999, 12, 31);
    public static final LocalDate RESIGNATION_DATE = LocalDate.of(2025, 12, 11);

    public static PscDao getPscDao() {
        PscDataDao pscDatadao = new PscDataDao();
        pscDatadao.setAppointmentId(APPOINTMENT_ID);
        pscDatadao.setCountry(ENGLAND.getDescription());
        pscDatadao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        pscDatadao.setDateOfBirth(DATE_OF_BIRTH);
        pscDatadao.setEtag(ETAG);
        pscDatadao.setForename(FORENAME);
        pscDatadao.setFormerNames(FORMER_NAMES);
        pscDatadao.setGoverningLaw(GOVERNING_LAW);
        pscDatadao.setKind(FILING_KIND_PSC);
        pscDatadao.setLegalEntityName(LEGAL_ENTITY_NAME);
        pscDatadao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        pscDatadao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
        pscDatadao.setLegalForm(LEGAL_FORM);
        pscDatadao.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
        pscDatadao.setNationality1(NATIONALITY1);
        pscDatadao.setNationality2(NATIONALITY2);
        List<String> nocList = List.of(NATURE_OF_CONTROL, NATURE_OF_CONTROL, NATURE_OF_CONTROL);
        pscDatadao.setNaturesOfControl(nocList);
        pscDatadao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
        pscDatadao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        pscDatadao.setResignationDate(RESIGNATION_DATE);
        pscDatadao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
        pscDatadao.setSurname(SURNAME);
        pscDatadao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));

        PscDao pscDao = new PscDao();
        pscDao.setId(ID);
        pscDao.setData(pscDatadao);
        return pscDao;
    }

    public static PscDao PscPersonDao() {
        PscDataDao pscDatadao = new PscDataDao();
        pscDatadao.setAppointmentId(APPOINTMENT_ID);
        pscDatadao.setKind(FILING_KIND_PSC);
        pscDatadao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        pscDatadao.setDateOfBirth(DATE_OF_BIRTH);
        pscDatadao.setEtag(ETAG);
        pscDatadao.setForename(FORENAME);
        pscDatadao.setSurname(SURNAME);
        pscDatadao.setFormerNames(FORMER_NAMES);
        pscDatadao.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
        pscDatadao.setNationality1(NATIONALITY1);
        pscDatadao.setNationality2(NATIONALITY2);
        pscDatadao.setNaturesOfControl(List.of(NATURE_OF_CONTROL, NATURE_OF_CONTROL, NATURE_OF_CONTROL));
        pscDatadao.setResignationDate(RESIGNATION_DATE);
        pscDatadao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
        pscDatadao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));

        PscDao pscDao = new PscDao();
        pscDao.setId(ID);
        pscDao.setData(pscDatadao);
        return pscDao;
    }

    public static PscDao PscLegalEntityDao() {
        PscDataDao pscDatadao = new PscDataDao();
        pscDatadao.setAppointmentId(APPOINTMENT_ID);
        pscDatadao.setKind(FILING_KIND_PSC);
        pscDatadao.setCountry(ENGLAND.getDescription());
        pscDatadao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        pscDatadao.setEtag(ETAG);
        pscDatadao.setGoverningLaw(GOVERNING_LAW);
        pscDatadao.setLegalEntityName(LEGAL_ENTITY_NAME);
        pscDatadao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        pscDatadao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
        pscDatadao.setLegalForm(LEGAL_FORM);
        pscDatadao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        pscDatadao.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
        pscDatadao.setNaturesOfControl(List.of(NATURE_OF_CONTROL, NATURE_OF_CONTROL, NATURE_OF_CONTROL));
        pscDatadao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
        pscDatadao.setResignationDate(RESIGNATION_DATE);
        pscDatadao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
        pscDatadao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));

        PscDao pscDao = new PscDao();
        pscDao.setId(ID);
        pscDao.setData(pscDatadao);
        return pscDao;
    }

    public static PscDto getPscDto() {
        PscDataDto pscDataDto = new PscDataDto();
        pscDataDto.setAppointmentId(APPOINTMENT_ID);
        pscDataDto.setCountry(ENGLAND);
        pscDataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        pscDataDto.setDateOfBirth(DATE_OF_BIRTH);
        pscDataDto.setForename(FORENAME);
        pscDataDto.setFormerNames(FORMER_NAMES);
        pscDataDto.setGoverningLaw(GOVERNING_LAW);
        pscDataDto.setKind(FILING_KIND_PSC);
        pscDataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
        pscDataDto.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        pscDataDto.setLegalEntityRegistrationLocation(ENGLAND);
        pscDataDto.setLegalForm(LEGAL_FORM);
        pscDataDto.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
        pscDataDto.setNationality1(BRITISH);
        pscDataDto.setNationality2(FRENCH);
        pscDataDto.setNaturesOfControl(List.of(NatureOfControl.TEST, NatureOfControl.TEST, NatureOfControl.TEST));
        pscDataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
        pscDataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        pscDataDto.setResignationDate(RESIGNATION_DATE);
        pscDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
        pscDataDto.setSurname(SURNAME);
        pscDataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));
        PscDto pscDto = new PscDto();
        pscDto.setData(pscDataDto);
        return pscDto;
    }

    public static PscDto legalEntityDto() {
        PscDataDto pscDataDto = new PscDataDto();
        pscDataDto.setAppointmentId(APPOINTMENT_ID);
        pscDataDto.setKind(FILING_KIND_PSC);
        pscDataDto.setCountry(ENGLAND);
        pscDataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        pscDataDto.setGoverningLaw(GOVERNING_LAW);
        pscDataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
        pscDataDto.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        pscDataDto.setLegalEntityRegistrationLocation(ENGLAND);
        pscDataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        pscDataDto.setLegalForm(LEGAL_FORM);
        pscDataDto.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
        pscDataDto.setNaturesOfControl(List.of(NatureOfControl.TEST, NatureOfControl.TEST, NatureOfControl.TEST));
        pscDataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
        pscDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
        pscDataDto.setResignationDate(RESIGNATION_DATE);

        PscDto pscDto = new PscDto();
        pscDto.setData(pscDataDto);
        return pscDto;
    }

    public static PscDto personPscDto() {
        PscDataDto pscDataDto = new PscDataDto();
        pscDataDto.setAppointmentId(APPOINTMENT_ID);
        pscDataDto.setKind(FILING_KIND_PSC);
        pscDataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        pscDataDto.setDateOfBirth(DATE_OF_BIRTH);
        pscDataDto.setForename(FORENAME);
        pscDataDto.setSurname(SURNAME);
        pscDataDto.setFormerNames(FORMER_NAMES);
        pscDataDto.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
        pscDataDto.setNationality1(BRITISH);
        pscDataDto.setNationality2(FRENCH);
        pscDataDto.setNaturesOfControl(List.of(NatureOfControl.TEST, NatureOfControl.TEST, NatureOfControl.TEST));
        pscDataDto.setResignationDate(RESIGNATION_DATE);
        pscDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
        pscDataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));

        PscDto pscDto = new PscDto();
        pscDto.setData(pscDataDto);
        return pscDto;
    }

    public static PscDataDto getPscDataDtoForPatch() {
        PscDataDto pscDataDto = new PscDataDto();
        pscDataDto.setCountry(FRANCE);
        pscDataDto.setForename("Bob");
        pscDataDto.setFormerNames("Former");
        pscDataDto.setNationality1(SPANISH);
        return pscDataDto;
    }

    private static AddressDao createAddressDao(String prefix) {
        AddressDao addressDao = new AddressDao();
        addressDao.setAddressLine1(prefix + ADDRESS_LINE1_SUFFIX);
        addressDao.setAddressLine2(prefix + ADDRESS_LINE2_SUFFIX);
        addressDao.setCountry(prefix + COUNTRY_SUFFIX);
        addressDao.setLocality(prefix + LOCALITY_SUFFIX);
        addressDao.setPostalCode(prefix + POSTAL_CODE_SUFFIX);
        addressDao.setPremises(prefix + PREMISES_SUFFIX);
        addressDao.setRegion(prefix + REGION_SUFFIX);
        return addressDao;
    }

    private static AddressDto createAddressDto(String prefix) {
        AddressDto addressDto = new AddressDto();
        addressDto.setAddressLine1(prefix + ADDRESS_LINE1_SUFFIX);
        addressDto.setAddressLine2(prefix + ADDRESS_LINE2_SUFFIX);
        addressDto.setCountry(prefix + COUNTRY_SUFFIX);
        addressDto.setLocality(prefix + LOCALITY_SUFFIX);
        addressDto.setPostalCode(prefix + POSTAL_CODE_SUFFIX);
        addressDto.setPremises(prefix + PREMISES_SUFFIX);
        addressDto.setRegion(prefix + REGION_SUFFIX);
        return addressDto;
    }
}
