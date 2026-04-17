package uk.gov.companieshouse.limitedpartnershipsapi.builder;


import com.google.common.collect.Lists;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country.ENGLAND;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country.FRANCE;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.BRITISH;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.FRENCH;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.SPANISH;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;

public class PersonWithSignificantControlBuilder {
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
    public static final List<String> NATURES_OF_CONTROL_LIST_DESCRIPTIONS = Lists.newArrayList(
            NatureOfControl.RLE.getDescription(),
            NatureOfControl.INDIVIDUAL_FIRM_CONTROL.getDescription(),
            NatureOfControl.ORP_TRUST_CONTROL.getDescription()
    );
    public static final List<NatureOfControl> NATURES_OF_CONTROL_LIST = Lists.newArrayList(
            NatureOfControl.RLE,
            NatureOfControl.INDIVIDUAL_FIRM_CONTROL,
            NatureOfControl.ORP_TRUST_CONTROL
    );

    public static class PersonWithSignificantControlDaoBuilder {

        private final PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDao();
        private final PersonWithSignificantControlDataDao personWithSignificantControlDataDao = new PersonWithSignificantControlDataDao();

        public PersonWithSignificantControlDaoBuilder personWithSignificantControlDao() {
            personWithSignificantControlDataDao.setAppointmentId(APPOINTMENT_ID);
            personWithSignificantControlDataDao.setCountry(ENGLAND.getDescription());
            personWithSignificantControlDataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            personWithSignificantControlDataDao.setDateOfBirth(DATE_OF_BIRTH);
            personWithSignificantControlDataDao.setEtag(ETAG);
            personWithSignificantControlDataDao.setForename(FORENAME);
            personWithSignificantControlDataDao.setFormerNames(FORMER_NAMES);
            personWithSignificantControlDataDao.setGoverningLaw(GOVERNING_LAW);
            personWithSignificantControlDataDao.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
            personWithSignificantControlDataDao.setLegalEntityName(LEGAL_ENTITY_NAME);
            personWithSignificantControlDataDao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
            personWithSignificantControlDataDao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
            personWithSignificantControlDataDao.setLegalForm(LEGAL_FORM);
            personWithSignificantControlDataDao.setNationality1(NATIONALITY1);
            personWithSignificantControlDataDao.setNationality2(NATIONALITY2);
            personWithSignificantControlDataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
            personWithSignificantControlDataDao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
            personWithSignificantControlDataDao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            personWithSignificantControlDataDao.setResignationDate(RESIGNATION_DATE);
            personWithSignificantControlDataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
            personWithSignificantControlDataDao.setSurname(SURNAME);
            personWithSignificantControlDataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));
            personWithSignificantControlDataDao.setType(PersonWithSignificantControlType.INDIVIDUAL_PERSON);

            personWithSignificantControlDao.setId(ID);
            personWithSignificantControlDao.setData(personWithSignificantControlDataDao);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder personPersonWithSignificantControlDao() {
            personWithSignificantControlDataDao.setAppointmentId(APPOINTMENT_ID);
            personWithSignificantControlDataDao.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
            personWithSignificantControlDataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            personWithSignificantControlDataDao.setDateOfBirth(DATE_OF_BIRTH);
            personWithSignificantControlDataDao.setEtag(ETAG);
            personWithSignificantControlDataDao.setForename(FORENAME);
            personWithSignificantControlDataDao.setSurname(SURNAME);
            personWithSignificantControlDataDao.setFormerNames(FORMER_NAMES);
            personWithSignificantControlDataDao.setNationality1(NATIONALITY1);
            personWithSignificantControlDataDao.setNationality2(NATIONALITY2);
            personWithSignificantControlDataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
            personWithSignificantControlDataDao.setResignationDate(RESIGNATION_DATE);
            personWithSignificantControlDataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
            personWithSignificantControlDataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));
            personWithSignificantControlDataDao.setType(PersonWithSignificantControlType.INDIVIDUAL_PERSON);

            personWithSignificantControlDao.setId(ID);
            personWithSignificantControlDao.setData(personWithSignificantControlDataDao);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder legalEntityPersonWithSignificantControlDao() {
            personWithSignificantControlDataDao.setAppointmentId(APPOINTMENT_ID);
            personWithSignificantControlDataDao.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
            personWithSignificantControlDataDao.setCountry(ENGLAND.getDescription());
            personWithSignificantControlDataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            personWithSignificantControlDataDao.setEtag(ETAG);
            personWithSignificantControlDataDao.setGoverningLaw(GOVERNING_LAW);
            personWithSignificantControlDataDao.setLegalEntityName(LEGAL_ENTITY_NAME);
            personWithSignificantControlDataDao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
            personWithSignificantControlDataDao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
            personWithSignificantControlDataDao.setLegalForm(LEGAL_FORM);
            personWithSignificantControlDataDao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            personWithSignificantControlDataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
            personWithSignificantControlDataDao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
            personWithSignificantControlDataDao.setResignationDate(RESIGNATION_DATE);
            personWithSignificantControlDataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
            personWithSignificantControlDataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));
            personWithSignificantControlDataDao.setType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY);

            personWithSignificantControlDao.setId(ID);
            personWithSignificantControlDao.setData(personWithSignificantControlDataDao);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder withKind(String kind) {
            this.personWithSignificantControlDataDao.setKind(kind);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder withNationality2(String nationality2) {
            this.personWithSignificantControlDataDao.setNationality2(nationality2);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder withPrincipalOfficeAddress(AddressDao principalOfficeAddress) {
            this.personWithSignificantControlDataDao.setPrincipalOfficeAddress(principalOfficeAddress);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder withUsualResidentialAddress(AddressDao usualResidentialAddress) {
            this.personWithSignificantControlDataDao.setUsualResidentialAddress(usualResidentialAddress);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder withServiceAddress(AddressDao serviceAddress) {
            this.personWithSignificantControlDataDao.setServiceAddress(serviceAddress);
            return this;
        }

        public PersonWithSignificantControlDaoBuilder withType(PersonWithSignificantControlType type) {
            this.personWithSignificantControlDataDao.setType(type);
            return this;
        }

        public PersonWithSignificantControlDao build() {
            personWithSignificantControlDao.setData(personWithSignificantControlDataDao);
            return personWithSignificantControlDao;
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


    public static class PersonWithSignificantControlDtoBuilder {
        private final PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlDto();
        private final PersonWithSignificantControlDataDto personWithSignificantControlDataDto = new PersonWithSignificantControlDataDto();

        public PersonWithSignificantControlDtoBuilder personWithSignificantControlDto() {
            personWithSignificantControlDataDto.setAppointmentId(APPOINTMENT_ID);
            personWithSignificantControlDataDto.setCountry(ENGLAND);
            personWithSignificantControlDataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            personWithSignificantControlDataDto.setDateOfBirth(DATE_OF_BIRTH);
            personWithSignificantControlDataDto.setForename(FORENAME);
            personWithSignificantControlDataDto.setFormerNames(FORMER_NAMES);
            personWithSignificantControlDataDto.setGoverningLaw(GOVERNING_LAW);
            personWithSignificantControlDataDto.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
            personWithSignificantControlDataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
            personWithSignificantControlDataDto.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
            personWithSignificantControlDataDto.setLegalEntityRegistrationLocation(ENGLAND);
            personWithSignificantControlDataDto.setLegalForm(LEGAL_FORM);
            personWithSignificantControlDataDto.setNationality1(BRITISH);
            personWithSignificantControlDataDto.setNationality2(FRENCH);
            personWithSignificantControlDataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
            personWithSignificantControlDataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
            personWithSignificantControlDataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            personWithSignificantControlDataDto.setResignationDate(RESIGNATION_DATE);
            personWithSignificantControlDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
            personWithSignificantControlDataDto.setSurname(SURNAME);
            personWithSignificantControlDataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));
            personWithSignificantControlDataDto.setType(PersonWithSignificantControlType.INDIVIDUAL_PERSON);

            return this;
        }

        public PersonWithSignificantControlDtoBuilder legalEntityPersonWithSignificantControlDto() {
            personWithSignificantControlDataDto.setAppointmentId(APPOINTMENT_ID);
            personWithSignificantControlDataDto.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
            personWithSignificantControlDataDto.setCountry(ENGLAND);
            personWithSignificantControlDataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            personWithSignificantControlDataDto.setGoverningLaw(GOVERNING_LAW);
            personWithSignificantControlDataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
            personWithSignificantControlDataDto.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
            personWithSignificantControlDataDto.setLegalEntityRegistrationLocation(ENGLAND);
            personWithSignificantControlDataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
            personWithSignificantControlDataDto.setLegalForm(LEGAL_FORM);
            personWithSignificantControlDataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
            personWithSignificantControlDataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
            personWithSignificantControlDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
            personWithSignificantControlDataDto.setResignationDate(RESIGNATION_DATE);
            personWithSignificantControlDataDto.setType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY);

            return this;
        }

        public PersonWithSignificantControlDtoBuilder personPersonWithSignificantControlDto() {
            personWithSignificantControlDataDto.setAppointmentId(APPOINTMENT_ID);
            personWithSignificantControlDataDto.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
            personWithSignificantControlDataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
            personWithSignificantControlDataDto.setDateOfBirth(DATE_OF_BIRTH);
            personWithSignificantControlDataDto.setForename(FORENAME);
            personWithSignificantControlDataDto.setSurname(SURNAME);
            personWithSignificantControlDataDto.setFormerNames(FORMER_NAMES);
            personWithSignificantControlDataDto.setNationality1(BRITISH);
            personWithSignificantControlDataDto.setNationality2(FRENCH);
            personWithSignificantControlDataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
            personWithSignificantControlDataDto.setResignationDate(RESIGNATION_DATE);
            personWithSignificantControlDataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
            personWithSignificantControlDataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));
            personWithSignificantControlDataDto.setType(PersonWithSignificantControlType.INDIVIDUAL_PERSON);

            return this;
        }

        public PersonWithSignificantControlDtoBuilder personWithSignificantControlDtoForPatch() {
            personWithSignificantControlDataDto.setCountry(FRANCE);
            personWithSignificantControlDataDto.setForename("Bob");
            personWithSignificantControlDataDto.setFormerNames("Former");
            personWithSignificantControlDataDto.setNationality1(SPANISH);
            return this;
        }

        public PersonWithSignificantControlDtoBuilder withKind(String kind) {
            this.personWithSignificantControlDataDto.setKind(kind);
            return this;
        }

        public PersonWithSignificantControlDtoBuilder withNationality1(Nationality nationality1) {
            this.personWithSignificantControlDataDto.setNationality1(nationality1);
            return this;
        }

        public PersonWithSignificantControlDtoBuilder withNationality2(Nationality nationality2) {
            this.personWithSignificantControlDataDto.setNationality2(nationality2);
            return this;
        }

        public PersonWithSignificantControlDtoBuilder withType(PersonWithSignificantControlType type) {
            this.personWithSignificantControlDataDto.setType(type);
            return this;
        }

        public PersonWithSignificantControlDto build() {
            personWithSignificantControlDto.setData(personWithSignificantControlDataDto);
            return personWithSignificantControlDto;
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
