package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.AddressDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.AddressDtoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDaoBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlDtoBuilder;
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

import static org.assertj.core.api.Assertions.assertThat;

import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country.ENGLAND;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL;


class PersonWithSignificantControlMapperTest {
    private static final PersonWithSignificantControlMapper MAPPER = Mappers.getMapper(PersonWithSignificantControlMapper.class);

    // Field name constants for extracting()
    private static final String FN_APPOINTMENT_ID = "appointmentId";
    private static final String FN_COUNTRY = "country";
    private static final String FN_DATE_EFFECTIVE_FROM = "dateEffectiveFrom";
    private static final String FN_DATE_OF_BIRTH = "dateOfBirth";
    private static final String FN_FORENAME = "forename";
    private static final String FN_FORMER_NAMES = "formerNames";
    private static final String FN_GOVERNING_LAW = "governingLaw";
    private static final String FN_KIND = "kind";
    private static final String FN_LEGAL_ENTITY_NAME = "legalEntityName";
    private static final String FN_LEGAL_ENTITY_REGISTER_NAME = "legalEntityRegisterName";
    private static final String FN_LEGAL_ENTITY_REGISTRATION_LOCATION = "legalEntityRegistrationLocation";
    private static final String FN_LEGAL_FORM = "legalForm";
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
    private static final String FN_TYPE = "type";
    private static final String APPOINTMENT_ID = "774744";
    private static final LocalDate DATE_EFFECTIVE_FROM = LocalDate.of(2020, 1, 1);
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1980, 1, 1);
    private static final String FORENAME = "John";
    private static final String FORMER_NAMES = "Johnny";
    private static final String GOVERNING_LAW = "English law";
    private static final String LEGAL_ENTITY_NAME = "John Doe Ltd";
    private static final String LEGAL_ENTITY_REGISTER_NAME = "UK Register";
    private static final String LEGAL_FORM = "Private Limited Company";
    private static final String REGISTERED_COMPANY_NUMBER = "12345678";
    private static final LocalDate RESIGNATION_DATE = LocalDate.of(2021, 1, 1);
    private static final String SURNAME = "Doe";
    private static final String POA_PREFIX = "POA_";
    private static final String SERVICE_PREFIX = "SERVICE_";
    private static final String URA_PREFIX = "URA_";
    private static final String ADDRESS_LINE1_SUFFIX = "Address Line 1";
    private static final String ADDRESS_LINE2_SUFFIX = "Address Line 2";
    private static final String COUNTRY_SUFFIX = "Country";
    private static final String LOCALITY_SUFFIX = "Locality";
    private static final String POSTAL_CODE_SUFFIX = "Postal Code";
    private static final String PREMISES_SUFFIX = "Premises";
    private static final String REGION_SUFFIX = "Region";
    private static final List<NatureOfControl> NATURES_OF_CONTROL_LIST = List.of(NatureOfControl.RLE, NatureOfControl.INDIVIDUAL_FIRM_CONTROL, NatureOfControl.ORP_TRUST_CONTROL);
    private static final List<String> NATURES_OF_CONTROL_DESC_LIST = List.of(NatureOfControl.RLE.getDescription(), NatureOfControl.INDIVIDUAL_FIRM_CONTROL.getDescription(), NatureOfControl.ORP_TRUST_CONTROL.getDescription());

    @Test
    void givenDao_whenMapsToDto_thenCorrect() {
        // given
        AddressDao principalOfficeAddress = buildAddressDao(POA_PREFIX);
        AddressDao serviceAddress = buildAddressDao(SERVICE_PREFIX);
        AddressDao usualResidentialAddress = buildAddressDao(URA_PREFIX);

        PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlDaoBuilder().
                withData(new PersonWithSignificantControlDaoBuilder.DataBuilder()
                        .withAppointmentId(APPOINTMENT_ID)
                        .withCountry(ENGLAND.getDescription())
                        .withDateEffectiveFrom(DATE_EFFECTIVE_FROM)
                        .withDateOfBirth(DATE_OF_BIRTH)
                        .withForename(FORENAME)
                        .withFormerNames(FORMER_NAMES)
                        .withGoverningLaw(GOVERNING_LAW)
                        .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                        .withLegalEntityName(LEGAL_ENTITY_NAME)
                        .withLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME)
                        .withLegalEntityRegistrationLocation(ENGLAND.getDescription())
                        .withLegalForm(LEGAL_FORM)
                        .withNationality1(Nationality.BRITISH.getDescription())
                        .withNationality2(Nationality.FRENCH.getDescription())
                        .withRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER)
                        .withResignationDate(RESIGNATION_DATE)
                        .withSurname(SURNAME)
                        .withType(PersonWithSignificantControlType.INDIVIDUAL_PERSON)
                        .withPrincipalOfficeAddress(principalOfficeAddress)
                        .withServiceAddress(serviceAddress)
                        .withUsualResidentialAddress(usualResidentialAddress)
                        .withNaturesOfControl(NATURES_OF_CONTROL_DESC_LIST)
                        .build())
                .build();

        // when
        PersonWithSignificantControlDto result = MAPPER.daoToDto(personWithSignificantControlDao);
        PersonWithSignificantControlDataDto dataDto = result.getData();

        // Grouped assertions for main fields
        assertThat(dataDto)
            .extracting(
                FN_APPOINTMENT_ID,
                FN_COUNTRY,
                FN_DATE_EFFECTIVE_FROM,
                FN_DATE_OF_BIRTH,
                FN_FORENAME,
                FN_FORMER_NAMES,
                FN_GOVERNING_LAW,
                FN_KIND,
                FN_LEGAL_ENTITY_NAME,
                FN_LEGAL_ENTITY_REGISTER_NAME,
                FN_LEGAL_ENTITY_REGISTRATION_LOCATION,
                FN_LEGAL_FORM,
                FN_NATIONALITY1,
                FN_NATIONALITY2,
                FN_REGISTERED_COMPANY_NUMBER,
                FN_RESIGNATION_DATE,
                FN_SURNAME,
                FN_TYPE
            )
            .containsExactly(
                APPOINTMENT_ID,
                ENGLAND,
                DATE_EFFECTIVE_FROM,
                DATE_OF_BIRTH,
                FORENAME,
                FORMER_NAMES,
                GOVERNING_LAW,
                FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                LEGAL_ENTITY_NAME,
                LEGAL_ENTITY_REGISTER_NAME,
                ENGLAND,
                LEGAL_FORM,
                Nationality.BRITISH.getDescription(),
                Nationality.FRENCH.getDescription(),
                REGISTERED_COMPANY_NUMBER,
                RESIGNATION_DATE,
                SURNAME,
                PersonWithSignificantControlType.INDIVIDUAL_PERSON
            );

        // Assert naturesOfControl
        assertThat(dataDto.getNaturesOfControl())
            .hasSize(3)
            .containsExactlyInAnyOrder(NatureOfControl.RLE, NatureOfControl.INDIVIDUAL_FIRM_CONTROL, NatureOfControl.ORP_TRUST_CONTROL);

        assertAddress(dataDto.getPrincipalOfficeAddress(), POA_PREFIX);
        assertAddress(dataDto.getServiceAddress(), SERVICE_PREFIX);
        assertAddress(dataDto.getUsualResidentialAddress(), URA_PREFIX);
    }

    @Test
    void givenDto_whenMapsToDao_thenCorrect() {
        // given
        AddressDto principalOfficeAddress = buildAddressDto(POA_PREFIX);
        AddressDto serviceAddress = buildAddressDto(SERVICE_PREFIX);
        AddressDto usualResidentialAddress = buildAddressDto(URA_PREFIX);

        PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlDtoBuilder()
                .withData(new PersonWithSignificantControlDtoBuilder.DataBuilder()
                        .withAppointmentId(APPOINTMENT_ID)
                        .withCountry(ENGLAND)
                        .withDateEffectiveFrom(DATE_EFFECTIVE_FROM)
                        .withDateOfBirth(DATE_OF_BIRTH)
                        .withForename(FORENAME)
                        .withFormerNames(FORMER_NAMES)
                        .withGoverningLaw(GOVERNING_LAW)
                        .withKind(FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL)
                        .withLegalEntityName(LEGAL_ENTITY_NAME)
                        .withLegalEntityRegisterName(LEGAL_ENTITY_REGISTER_NAME)
                        .withLegalEntityRegistrationLocation(ENGLAND)
                        .withLegalForm(LEGAL_FORM)
                        .withNationality1(Nationality.BRITISH)
                        .withNationality2(Nationality.FRENCH)
                        .withRegisteredCompanyNumber(REGISTERED_COMPANY_NUMBER)
                        .withResignationDate(RESIGNATION_DATE)
                        .withSurname(SURNAME)
                        .withType(PersonWithSignificantControlType.INDIVIDUAL_PERSON)
                        .withPrincipalOfficeAddress(principalOfficeAddress)
                        .withServiceAddress(serviceAddress)
                        .withUsualResidentialAddress(usualResidentialAddress)
                        .withNaturesOfControl(NATURES_OF_CONTROL_LIST)
                        .build())
                .build();


        // when
        PersonWithSignificantControlDao result = MAPPER.dtoToDao(personWithSignificantControlDto);
        PersonWithSignificantControlDataDao daoData = result.getData();

        // Grouped assertions for main fields
        assertThat(daoData)
            .extracting(
                FN_APPOINTMENT_ID,
                FN_COUNTRY,
                FN_DATE_EFFECTIVE_FROM,
                FN_DATE_OF_BIRTH,
                FN_FORENAME,
                FN_FORMER_NAMES,
                FN_GOVERNING_LAW,
                FN_KIND,
                FN_LEGAL_ENTITY_NAME,
                FN_LEGAL_ENTITY_REGISTER_NAME,
                FN_LEGAL_ENTITY_REGISTRATION_LOCATION,
                FN_LEGAL_FORM,
                FN_NATIONALITY1,
                FN_NATIONALITY2,
                FN_REGISTERED_COMPANY_NUMBER,
                FN_RESIGNATION_DATE,
                FN_SURNAME,
                FN_TYPE
            )
            .containsExactly(
                APPOINTMENT_ID,
                ENGLAND.getDescription(),
                DATE_EFFECTIVE_FROM,
                DATE_OF_BIRTH,
                FORENAME,
                FORMER_NAMES,
                GOVERNING_LAW,
                FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                LEGAL_ENTITY_NAME,
                LEGAL_ENTITY_REGISTER_NAME,
                ENGLAND.getDescription(),
                LEGAL_FORM,
                Nationality.BRITISH.getDescription(),
                Nationality.FRENCH.getDescription(),
                REGISTERED_COMPANY_NUMBER,
                RESIGNATION_DATE,
                SURNAME,
                PersonWithSignificantControlType.INDIVIDUAL_PERSON
            );

        // Assert naturesOfControl
        assertThat(daoData.getNaturesOfControl())
            .hasSize(3)
            .allMatch(NATURES_OF_CONTROL_DESC_LIST::contains);

        assertAddress(daoData.getPrincipalOfficeAddress(), POA_PREFIX);
        assertAddress(daoData.getServiceAddress(), SERVICE_PREFIX);
        assertAddress(daoData.getUsualResidentialAddress(), URA_PREFIX);
    }

    private AddressDao buildAddressDao(String prefix) {
        return new AddressDaoBuilder()
                .withAddressLine1(prefix + ADDRESS_LINE1_SUFFIX)
                .withAddressLine2(prefix + ADDRESS_LINE2_SUFFIX)
                .withCountry(prefix + COUNTRY_SUFFIX)
                .withLocality(prefix + LOCALITY_SUFFIX)
                .withPostalCode(prefix + POSTAL_CODE_SUFFIX)
                .withPremises(prefix + PREMISES_SUFFIX)
                .withRegion(prefix + REGION_SUFFIX)
                .build();
    }

    private AddressDto buildAddressDto(String prefix) {
        return new AddressDtoBuilder()
                .withAddressLine1(prefix + ADDRESS_LINE1_SUFFIX)
                .withAddressLine2(prefix + ADDRESS_LINE2_SUFFIX)
                .withCountry(prefix + COUNTRY_SUFFIX)
                .withLocality(prefix + LOCALITY_SUFFIX)
                .withPostalCode(prefix + POSTAL_CODE_SUFFIX)
                .withPremises(prefix + PREMISES_SUFFIX)
                .withRegion(prefix + REGION_SUFFIX)
                .build();
    }

    private void assertAddress(Object address, String prefix) {
        assertThat(address)
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
                        prefix + ADDRESS_LINE1_SUFFIX,
                        prefix + ADDRESS_LINE2_SUFFIX,
                        prefix + COUNTRY_SUFFIX,
                        prefix + LOCALITY_SUFFIX,
                        prefix + POSTAL_CODE_SUFFIX,
                        prefix + PREMISES_SUFFIX,
                        prefix + REGION_SUFFIX
                );
    }

}
