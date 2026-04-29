package uk.gov.companieshouse.limitedpartnershipsapi.builder;


import com.google.common.collect.Lists;
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
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.BRITISH;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality.FRENCH;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;

public class PersonWithSignificantControlBuilder {
    public static final String PERSON_WITH_SIGNIFICANT_CONTROL_ID = "1234";
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

    public PersonWithSignificantControlDto individualPersonDto () {
        PersonWithSignificantControlDto dto = new PersonWithSignificantControlDto();
        dto.setId(PERSON_WITH_SIGNIFICANT_CONTROL_ID);

        PersonWithSignificantControlDataDto dataDto = new PersonWithSignificantControlDataDto();
        dataDto.setAppointmentId(APPOINTMENT_ID);
        dataDto.setCountry(ENGLAND);
        dataDto.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        dataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        dataDto.setDateOfBirth(DATE_OF_BIRTH);
        dataDto.setForename(FORENAME);
        dataDto.setSurname(SURNAME);
        dataDto.setFormerNames(FORMER_NAMES);
        dataDto.setNationality1(BRITISH);
        dataDto.setNationality2(FRENCH);
        dataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
        dataDto.setResignationDate(RESIGNATION_DATE);
        dataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
        dataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));
        dataDto.setType(PersonWithSignificantControlType.INDIVIDUAL_PERSON);

        dto.setData(dataDto);
        return dto;
    }

    public PersonWithSignificantControlDto relevantLegalEntityDto() {
        PersonWithSignificantControlDto dto = new PersonWithSignificantControlDto();
        dto.setId(PERSON_WITH_SIGNIFICANT_CONTROL_ID);

        PersonWithSignificantControlDataDto dataDto = new PersonWithSignificantControlDataDto();
        dataDto.setAppointmentId(APPOINTMENT_ID);
        dataDto.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        dataDto.setCountry(ENGLAND);
        dataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        dataDto.setGoverningLaw(GOVERNING_LAW);
        dataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
        dataDto.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        dataDto.setLegalEntityRegistrationLocation(ENGLAND);
        dataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        dataDto.setLegalForm(LEGAL_FORM);
        dataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
        dataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
        dataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
        dataDto.setResignationDate(RESIGNATION_DATE);
        dataDto.setType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY);

        dto.setData(dataDto);
        return dto;
    }

    public PersonWithSignificantControlDto otherRegistrablePersonDto() {
        PersonWithSignificantControlDto dto = new PersonWithSignificantControlDto();
        dto.setId(PERSON_WITH_SIGNIFICANT_CONTROL_ID);

        PersonWithSignificantControlDataDto dataDto = new PersonWithSignificantControlDataDto();
        dataDto.setAppointmentId(APPOINTMENT_ID);
        dataDto.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        dataDto.setCountry(ENGLAND);
        dataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        dataDto.setGoverningLaw(GOVERNING_LAW);
        dataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
        dataDto.setLegalForm(LEGAL_FORM);
        dataDto.setNaturesOfControl(NATURES_OF_CONTROL_LIST);
        dataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
        dataDto.setResignationDate(RESIGNATION_DATE);
        dataDto.setType(PersonWithSignificantControlType.OTHER_REGISTRABLE_PERSON);

        dto.setData(dataDto);
        return dto;
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

    public PersonWithSignificantControlDao individualPersonDao() {
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlDao();
        dao.setId(PERSON_WITH_SIGNIFICANT_CONTROL_ID);
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

        PersonWithSignificantControlDataDao dataDao = new PersonWithSignificantControlDataDao();
        dataDao.setAppointmentId(APPOINTMENT_ID);
        dataDao.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        dataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        dataDao.setDateOfBirth(DATE_OF_BIRTH);
        dataDao.setEtag(ETAG);
        dataDao.setForename(FORENAME);
        dataDao.setSurname(SURNAME);
        dataDao.setFormerNames(FORMER_NAMES);
        dataDao.setNationality1(NATIONALITY1);
        dataDao.setNationality2(NATIONALITY2);
        dataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
        dataDao.setResignationDate(RESIGNATION_DATE);
        dataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
        dataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));
        dataDao.setType(PersonWithSignificantControlType.INDIVIDUAL_PERSON);

        dao.setData(dataDao);
        return dao;
    }

    public PersonWithSignificantControlDao relevantLegalEntityDao() {
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlDao();
        dao.setId(PERSON_WITH_SIGNIFICANT_CONTROL_ID);
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

        PersonWithSignificantControlDataDao dataDao = new PersonWithSignificantControlDataDao();
        dataDao.setAppointmentId(APPOINTMENT_ID);
        dataDao.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        dataDao.setCountry(ENGLAND.getDescription());
        dataDao.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        dataDao.setEtag(ETAG);
        dataDao.setGoverningLaw(GOVERNING_LAW);
        dataDao.setLegalEntityName(LEGAL_ENTITY_NAME);
        dataDao.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        dataDao.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
        dataDao.setLegalForm(LEGAL_FORM);
        dataDao.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        dataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
        dataDao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
        dataDao.setResignationDate(RESIGNATION_DATE);
        dataDao.setServiceAddress(createAddressDao(SERVICE_PREFIX));
        dataDao.setUsualResidentialAddress(createAddressDao(URA_PREFIX));
        dataDao.setType(PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY);

        dao.setData(dataDao);
        return dao;
    }

    public PersonWithSignificantControlDao otherRegistrablePersonDao() {
        PersonWithSignificantControlDao dao = new PersonWithSignificantControlDao();
        dao.setId(PERSON_WITH_SIGNIFICANT_CONTROL_ID);
        dao.setTransactionId(TransactionBuilder.TRANSACTION_ID);

        PersonWithSignificantControlDataDao dataDao = new PersonWithSignificantControlDataDao();
        dataDao.setAppointmentId(APPOINTMENT_ID);
        dataDao.setKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL);
        dataDao.setCountry(ENGLAND.getDescription());
        dataDao.setEtag(ETAG);
        dataDao.setGoverningLaw(GOVERNING_LAW);
        dataDao.setLegalEntityName(LEGAL_ENTITY_NAME);
        dataDao.setLegalForm(LEGAL_FORM);
        dataDao.setNaturesOfControl(NATURES_OF_CONTROL_LIST_DESCRIPTIONS);
        dataDao.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
        dataDao.setType(PersonWithSignificantControlType.OTHER_REGISTRABLE_PERSON);

        dao.setData(dataDao);
        return dao;
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
