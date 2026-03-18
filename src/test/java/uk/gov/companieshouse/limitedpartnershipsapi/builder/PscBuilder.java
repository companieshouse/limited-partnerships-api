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
    public static final String SURNAME = "Smith";
    public static final String GOVERNING_LAW = "law of england";
    public static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    public static final String LEGAL_ENTITY_REGISTER_NAME = "Legal Entity Register Name";
    public static final String LEGAL_ENTITY_REGISTRATION_LOCATION = "England";
    public static final String LEGAL_FORM = "Legal Form";
    public static final String NATIONALITY1 = "British";
    public static final String NATIONALITY2 = "French";
    public static final String REGISTERED_COMPANY_NUMBER = "12345678";
    public static final boolean LEGAL_PERSONALITY_STATEMENT_CHECKED = true;
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
    public static final List<String> NATURES_OF_CONTROL_LIST_DESCRIPTIONS = List.of(
            NatureOfControl.RLE.getDescription(),
            NatureOfControl.INDIVIDUAL_FIRM_CONTROL.getDescription(),
            NatureOfControl.ORP_TRUST_CONTROL.getDescription()
    );
    public static final List<NatureOfControl> NATURES_OF_CONTROL_LIST = List.of(
            NatureOfControl.RLE,
            NatureOfControl.INDIVIDUAL_FIRM_CONTROL,
            NatureOfControl.ORP_TRUST_CONTROL
    );


    public static class PscDaoBuilder {

        private final PscDao pscDao = new PscDao();
        private final PscDataDao pscDataDao = new PscDataDao();

        public PscDaoBuilder pscDao() {
            pscDataDao.setAppointmentId(APPOINTMENT_ID);
            pscDataDao.setCountry(ENGLAND.getDescription());
            pscDataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            pscDataDao.setDateOfBirth(DATE_OF_BIRTH);
            pscDataDao.setEtag(ETAG);
            pscDataDao.setForename(FORENAME);
            pscDataDao.setFormerNames(FORMER_NAMES);
            pscDataDao.setGoverningLaw(GOVERNING_LAW);
            pscDataDao.setKind(FILING_KIND_PSC);
            pscDataDao.setLegalEntityName(LEGAL_ENTITY_NAME);
            pscDataDao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
            pscDataDao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
            pscDataDao.setLegalForm(LEGAL_FORM);
            pscDataDao.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
            pscDataDao.setNationality1(NATIONALITY1);
            pscDataDao.setNationality2(NATIONALITY2);
            pscDataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
            pscDataDao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
            pscDataDao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            pscDataDao.setResignationDate(RESIGNATION_DATE);
            pscDataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
            pscDataDao.setSurname(SURNAME);
            pscDataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));

            pscDao.setId(ID);
            pscDao.setData(pscDataDao);
            return this;
        }

        public PscDaoBuilder pscPersonDao() {
            pscDataDao.setAppointmentId(APPOINTMENT_ID);
            pscDataDao.setKind(FILING_KIND_PSC);
            pscDataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            pscDataDao.setDateOfBirth(DATE_OF_BIRTH);
            pscDataDao.setEtag(ETAG);
            pscDataDao.setForename(FORENAME);
            pscDataDao.setSurname(SURNAME);
            pscDataDao.setFormerNames(FORMER_NAMES);
            pscDataDao.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
            pscDataDao.setNationality1(NATIONALITY1);
            pscDataDao.setNationality2(NATIONALITY2);
            pscDataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
            pscDataDao.setResignationDate(RESIGNATION_DATE);
            pscDataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
            pscDataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));

            pscDao.setId(ID);
            pscDao.setData(pscDataDao);
            return this;
        }

        public PscDaoBuilder pscLegalEntityDao() {
            pscDataDao.setAppointmentId(APPOINTMENT_ID);
            pscDataDao.setKind(FILING_KIND_PSC);
            pscDataDao.setCountry(ENGLAND.getDescription());
            pscDataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            pscDataDao.setEtag(ETAG);
            pscDataDao.setGoverningLaw(GOVERNING_LAW);
            pscDataDao.setLegalEntityName(LEGAL_ENTITY_NAME);
            pscDataDao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
            pscDataDao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
            pscDataDao.setLegalForm(LEGAL_FORM);
            pscDataDao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            pscDataDao.setLegalPersonalityStatementChecked(LEGAL_PERSONALITY_STATEMENT_CHECKED);
            pscDataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
            pscDataDao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
            pscDataDao.setResignationDate(RESIGNATION_DATE);
            pscDataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
            pscDataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));

            pscDao.setId(ID);
            pscDao.setData(pscDataDao);
            return this;
        }

        public PscDaoBuilder withKind(String kind) {
            this.pscDataDao.setKind(kind);
            return this;
        }

        public PscDao build() {
            pscDao.setData(pscDataDao);
            return pscDao;
        }

        private AddressDao createAddressDao(String prefix) {
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
    }


    public static class PscDtoBuilder {
        private final PscDto pscDto = new PscDto();
        private final PscDataDto pscDataDto = new PscDataDto();

        public PscDtoBuilder pscDto() {
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
            pscDataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
            pscDataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
            pscDataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            pscDataDto.setResignationDate(RESIGNATION_DATE);
            pscDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
            pscDataDto.setSurname(SURNAME);
            pscDataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));

            return this;
        }

        public PscDtoBuilder legalEntityDto() {
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
            pscDataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
            pscDataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
            pscDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
            pscDataDto.setResignationDate(RESIGNATION_DATE);

            return this;
        }

        public PscDtoBuilder personPscDto() {
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
            pscDataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
            pscDataDto.setResignationDate(RESIGNATION_DATE);
            pscDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
            pscDataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));

            return this;
        }

        public PscDtoBuilder pscDtoForPatch() {
            pscDataDto.setCountry(FRANCE);
            pscDataDto.setForename("Bob");
            pscDataDto.setFormerNames("Former");
            pscDataDto.setNationality1(SPANISH);
            return this;
        }

        public PscDtoBuilder withKind(String kind) {
            this.pscDataDto.setKind(kind);
            return this;
        }

        public PscDto build() {
            pscDto.setData(pscDataDto);
            return pscDto;
        }

        private AddressDto createAddressDto(String prefix) {
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
}
