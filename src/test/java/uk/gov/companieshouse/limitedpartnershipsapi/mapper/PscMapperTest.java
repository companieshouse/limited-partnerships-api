package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dao.AddressDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.dto.AddressDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode.REGISTRATION;

class PscMapperTest {
    private static final String ETAG = "eTag";
    private static final String APPOINTMENT_ID = "1234";
    private static final String COUNTRY = "England";
    private static final String FORENAME = "John";
    private static final String FORMER_NAMES = "Doe";
    private static final String GOVERNING_LAW = "law of england";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String LEGAL_ENTITY_REGISTER_NAME = "Legal Entity Register Name";
    private static final String LEGAL_ENTITY_REGISTRATION_LOCATION = "England";
    private static final String LEGAL_FORM = "Legal Form";
    private static final String NATIONALITY1 = "British";
    private static final String NATIONALITY2 = "French";
    private static final String REGISTERED_COMPANY_NUMBER = "12345678";
    private static final String SURNAME = "Smith";
    private static final String NATURE_OF_CONTROL = "test";
    private static final String POA_PREFIX = "poa";
    private static final String SERVICE_PREFIX = "service";
    private static final String URA_PREFIX = "ura";
    private static final String ADDRESS_LINE1_SUFFIX = " line1";
    private static final String ADDRESS_LINE2_SUFFIX = " line2";
    private static final String COUNTRY_SUFFIX = " England";
    private static final String LOCALITY_SUFFIX = " Birmingham";
    private static final String POSTAL_CODE_SUFFIX = " BM1 2EH";
    private static final String PREMISES_SUFFIX = " 22";
    private static final String REGION_SUFFIX = " West Midlands";
    private static final LocalDate DATE_EFFECTIVE_FROM = LocalDate.of(2026, 1, 20);
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1999, 12, 31);
    private static final LocalDate RESIGNATION_DATE = LocalDate.of(2025, 12, 11);

    // Field name constants for extracting()
    private static final String FN_APPOINTMENT_ID = "appointmentId";
    private static final String FN_COUNTRY = "country";
    private static final String FN_DATE_EFFECTIVE_FROM = "dateEffectiveFrom";
    private static final String FN_DATE_OF_BIRTH = "dateOfBirth";
    private static final String FN_ETAG = "etag";
    private static final String FN_FORENAME = "forename";
    private static final String FN_FORMER_NAMES = "formerNames";
    private static final String FN_GOVERNING_LAW = "governingLaw";
    private static final String FN_KIND = "kind";
    private static final String FN_LEGAL_ENTITY_NAME = "legalEntityName";
    private static final String FN_LEGAL_ENTITY_REGISTER_NAME = "legalEntityRegisterName";
    private static final String FN_LEGAL_ENTITY_REGISTRATION_LOCATION = "legalEntityRegistrationLocation";
    private static final String FN_LEGAL_FORM = "legalForm";
    private static final String FN_LEGAL_PERSONALITY_STATEMENT_CHECKED = "legalPersonalityStatementChecked";
    private static final String FN_NATIONALITY1 = "nationality1";
    private static final String FN_NATIONALITY2 = "nationality2";
    private static final String FN_REGISTERED_COMPANY_NUMBER = "registeredCompanyNumber";
    private static final String FN_RESIGNATION_DATE = "resignationDate";
    private static final String FN_SURNAME = "surname";
    private static final String FN_ADDRESS_LINE1 = "addressLine1";
    private static final String FN_ADDRESS_LINE2 = "addressLine2";
    private static final String FN_LOCALITY = "locality";
    private static final String FN_POSTAL_CODE = "postalCode";
    private static final String FN_PREMISES = "premises";
    private static final String FN_REGION = "region";


    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        PscDataDao sourceData = new PscDataDao();
        // set data fields
        sourceData.setAppointmentId(APPOINTMENT_ID);
        sourceData.setCountry(COUNTRY);
        sourceData.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        sourceData.setDateOfBirth(DATE_OF_BIRTH);
        sourceData.setEtag(ETAG);
        sourceData.setForename(FORENAME);
        sourceData.setFormerNames(FORMER_NAMES);
        sourceData.setGoverningLaw(GOVERNING_LAW);
        sourceData.setKind(REGISTRATION.getDescription());
        sourceData.setLegalEntityName(LEGAL_ENTITY_NAME);
        sourceData.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        sourceData.setLegalEntityRegistrationLocation(LEGAL_ENTITY_REGISTRATION_LOCATION);
        sourceData.setLegalForm(LEGAL_FORM);
        sourceData.setLegalPersonalityStatementChecked(true);
        sourceData.setNationality1(NATIONALITY1);
        sourceData.setNationality2(NATIONALITY2);
        List<String> nocList = List.of(NATURE_OF_CONTROL, NATURE_OF_CONTROL, NATURE_OF_CONTROL);
        sourceData.setNaturesOfControl(nocList);
        sourceData.setPrincipalOfficeAddress(createAddressDao(POA_PREFIX));
        sourceData.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        sourceData.setResignationDate(RESIGNATION_DATE);
        sourceData.setServiceAddress(createAddressDao(SERVICE_PREFIX));
        sourceData.setSurname(SURNAME);
        sourceData.setUsualResidentialAddress(createAddressDao(URA_PREFIX));
        PscDao source = new PscDao();
        source.setData(sourceData);

        // when
        PscDto result = PscMapper.INSTANCE.daoToDto(source);
        PscDataDto dataDto = result.getData();

        // Grouped assertions for main fields
        assertThat(dataDto)
            .extracting(
                FN_APPOINTMENT_ID,
                FN_COUNTRY,
                FN_DATE_EFFECTIVE_FROM,
                FN_DATE_OF_BIRTH,
                FN_ETAG,
                FN_FORENAME,
                FN_FORMER_NAMES,
                FN_GOVERNING_LAW,
                FN_KIND,
                FN_LEGAL_ENTITY_NAME,
                FN_LEGAL_ENTITY_REGISTER_NAME,
                FN_LEGAL_ENTITY_REGISTRATION_LOCATION,
                FN_LEGAL_FORM,
                FN_LEGAL_PERSONALITY_STATEMENT_CHECKED,
                FN_NATIONALITY1,
                FN_NATIONALITY2,
                FN_REGISTERED_COMPANY_NUMBER,
                FN_RESIGNATION_DATE,
                FN_SURNAME
            )
            .containsExactly(
                APPOINTMENT_ID,
                Country.ENGLAND,
                DATE_EFFECTIVE_FROM,
                DATE_OF_BIRTH,
                ETAG,
                FORENAME,
                FORMER_NAMES,
                GOVERNING_LAW,
                REGISTRATION.getDescription(),
                LEGAL_ENTITY_NAME,
                LEGAL_ENTITY_REGISTER_NAME,
                Country.ENGLAND,
                LEGAL_FORM,
                true,
                Nationality.BRITISH,
                Nationality.FRENCH,
                REGISTERED_COMPANY_NUMBER,
                RESIGNATION_DATE,
                SURNAME
            );

        // Assert naturesOfControl
        assertThat(dataDto.getNaturesOfControl())
            .hasSize(3)
            .containsExactlyInAnyOrder(NatureOfControl.TEST, NatureOfControl.TEST, NatureOfControl.TEST);

        // Grouped assertions for address fields
        assertThat(dataDto.getPrincipalOfficeAddress())
            .extracting(
                FN_ADDRESS_LINE1,
                FN_ADDRESS_LINE2,
                FN_COUNTRY,
                FN_LOCALITY,
                FN_POSTAL_CODE,
                FN_PREMISES,
                FN_REGION
            )
            .containsExactly(
                POA_PREFIX + ADDRESS_LINE1_SUFFIX,
                POA_PREFIX + ADDRESS_LINE2_SUFFIX,
                POA_PREFIX + COUNTRY_SUFFIX,
                POA_PREFIX + LOCALITY_SUFFIX,
                POA_PREFIX + POSTAL_CODE_SUFFIX,
                POA_PREFIX + PREMISES_SUFFIX,
                POA_PREFIX + REGION_SUFFIX
            );

        assertThat(dataDto.getServiceAddress())
            .extracting(
                FN_ADDRESS_LINE1,
                FN_ADDRESS_LINE2,
                FN_COUNTRY,
                FN_LOCALITY,
                FN_POSTAL_CODE,
                FN_PREMISES,
                FN_REGION
            )
            .containsExactly(
                SERVICE_PREFIX + ADDRESS_LINE1_SUFFIX,
                SERVICE_PREFIX + ADDRESS_LINE2_SUFFIX,
                SERVICE_PREFIX + COUNTRY_SUFFIX,
                SERVICE_PREFIX + LOCALITY_SUFFIX,
                SERVICE_PREFIX + POSTAL_CODE_SUFFIX,
                SERVICE_PREFIX + PREMISES_SUFFIX,
                SERVICE_PREFIX + REGION_SUFFIX
            );

        assertThat(dataDto.getUsualResidentialAddress())
            .extracting(
                FN_ADDRESS_LINE1,
                FN_ADDRESS_LINE2,
                FN_COUNTRY,
                FN_LOCALITY,
                FN_POSTAL_CODE,
                FN_PREMISES,
                FN_REGION
            )
            .containsExactly(
                URA_PREFIX + ADDRESS_LINE1_SUFFIX,
                URA_PREFIX + ADDRESS_LINE2_SUFFIX,
                URA_PREFIX + COUNTRY_SUFFIX,
                URA_PREFIX + LOCALITY_SUFFIX,
                URA_PREFIX + POSTAL_CODE_SUFFIX,
                URA_PREFIX + PREMISES_SUFFIX,
                URA_PREFIX + REGION_SUFFIX
            );
    }

    @Test
    void givenDto_whenMapsToDao_thenCorrect() {
        // given
        PscDataDto dataDto = new PscDataDto();
        dataDto.setAppointmentId(APPOINTMENT_ID);
        dataDto.setCountry(Country.ENGLAND);
        dataDto.setDateEffectiveFrom(DATE_EFFECTIVE_FROM);
        dataDto.setDateOfBirth(DATE_OF_BIRTH);
        dataDto.setEtag(ETAG);
        dataDto.setForename(FORENAME);
        dataDto.setFormerNames(FORMER_NAMES);
        dataDto.setGoverningLaw(GOVERNING_LAW);
        dataDto.setKind(REGISTRATION.getDescription());
        dataDto.setLegalEntityName(LEGAL_ENTITY_NAME);
        dataDto.setLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME);
        dataDto.setLegalEntityRegistrationLocation(Country.ENGLAND);
        dataDto.setLegalForm(LEGAL_FORM);
        dataDto.setLegalPersonalityStatementChecked(true);
        dataDto.setNationality1(Nationality.BRITISH);
        dataDto.setNationality2(Nationality.FRENCH);
        List<NatureOfControl> nocList = List.of(NatureOfControl.TEST, NatureOfControl.TEST, NatureOfControl.TEST);
        dataDto.setNaturesOfControl(nocList);
        dataDto.setPrincipalOfficeAddress(createAddressDto(POA_PREFIX));
        dataDto.setRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER);
        dataDto.setResignationDate(RESIGNATION_DATE);
        dataDto.setServiceAddress(createAddressDto(SERVICE_PREFIX));
        dataDto.setSurname(SURNAME);
        dataDto.setUsualResidentialAddress(createAddressDto(URA_PREFIX));
        PscDto dto = new PscDto();
        dto.setData(dataDto);

        // when
        PscDao result = PscMapper.INSTANCE.dtoToDao(dto);
        PscDataDao daoData = result.getData();

        // Grouped assertions for main fields
        assertThat(daoData)
            .extracting(
                FN_APPOINTMENT_ID,
                FN_COUNTRY,
                FN_DATE_EFFECTIVE_FROM,
                FN_DATE_OF_BIRTH,
                FN_ETAG,
                FN_FORENAME,
                FN_FORMER_NAMES,
                FN_GOVERNING_LAW,
                FN_KIND,
                FN_LEGAL_ENTITY_NAME,
                FN_LEGAL_ENTITY_REGISTER_NAME,
                FN_LEGAL_ENTITY_REGISTRATION_LOCATION,
                FN_LEGAL_FORM,
                FN_LEGAL_PERSONALITY_STATEMENT_CHECKED,
                FN_NATIONALITY1,
                FN_NATIONALITY2,
                FN_REGISTERED_COMPANY_NUMBER,
                FN_RESIGNATION_DATE,
                FN_SURNAME
            )
            .containsExactly(
                APPOINTMENT_ID,
                COUNTRY,
                DATE_EFFECTIVE_FROM,
                DATE_OF_BIRTH,
                ETAG,
                FORENAME,
                FORMER_NAMES,
                GOVERNING_LAW,
                REGISTRATION.getDescription(),
                LEGAL_ENTITY_NAME,
                LEGAL_ENTITY_REGISTER_NAME,
                LEGAL_ENTITY_REGISTRATION_LOCATION,
                LEGAL_FORM,
                true,
                NATIONALITY1,
                NATIONALITY2,
                REGISTERED_COMPANY_NUMBER,
                RESIGNATION_DATE,
                SURNAME
            );

        // Assert naturesOfControl
        assertThat(daoData.getNaturesOfControl())
            .hasSize(3)
            .containsExactlyInAnyOrder(NATURE_OF_CONTROL, NATURE_OF_CONTROL, NATURE_OF_CONTROL);

        // Grouped assertions for address fields
        assertThat(daoData.getPrincipalOfficeAddress())
            .extracting(
                FN_ADDRESS_LINE1,
                FN_ADDRESS_LINE2,
                FN_COUNTRY,
                FN_LOCALITY,
                FN_POSTAL_CODE,
                FN_PREMISES,
                FN_REGION
            )
            .containsExactly(
                POA_PREFIX + ADDRESS_LINE1_SUFFIX,
                POA_PREFIX + ADDRESS_LINE2_SUFFIX,
                POA_PREFIX + COUNTRY_SUFFIX,
                POA_PREFIX + LOCALITY_SUFFIX,
                POA_PREFIX + POSTAL_CODE_SUFFIX,
                POA_PREFIX + PREMISES_SUFFIX,
                POA_PREFIX + REGION_SUFFIX
            );

        assertThat(daoData.getServiceAddress())
            .extracting(
                FN_ADDRESS_LINE1,
                FN_ADDRESS_LINE2,
                FN_COUNTRY,
                FN_LOCALITY,
                FN_POSTAL_CODE,
                FN_PREMISES,
                FN_REGION
            )
            .containsExactly(
                SERVICE_PREFIX + ADDRESS_LINE1_SUFFIX,
                SERVICE_PREFIX + ADDRESS_LINE2_SUFFIX,
                SERVICE_PREFIX + COUNTRY_SUFFIX,
                SERVICE_PREFIX + LOCALITY_SUFFIX,
                SERVICE_PREFIX + POSTAL_CODE_SUFFIX,
                SERVICE_PREFIX + PREMISES_SUFFIX,
                SERVICE_PREFIX + REGION_SUFFIX
            );

        assertThat(daoData.getUsualResidentialAddress())
            .extracting(
                FN_ADDRESS_LINE1,
                FN_ADDRESS_LINE2,
                FN_COUNTRY,
                FN_LOCALITY,
                FN_POSTAL_CODE,
                FN_PREMISES,
                FN_REGION
            )
            .containsExactly(
                URA_PREFIX + ADDRESS_LINE1_SUFFIX,
                URA_PREFIX + ADDRESS_LINE2_SUFFIX,
                URA_PREFIX + COUNTRY_SUFFIX,
                URA_PREFIX + LOCALITY_SUFFIX,
                URA_PREFIX + POSTAL_CODE_SUFFIX,
                URA_PREFIX + PREMISES_SUFFIX,
                URA_PREFIX + REGION_SUFFIX
            );
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
