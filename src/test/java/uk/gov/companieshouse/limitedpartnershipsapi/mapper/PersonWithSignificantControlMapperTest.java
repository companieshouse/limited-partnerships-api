package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.PersonWithSignificantControlType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dao.PersonWithSignificantControlDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.personwithsignificantcontrol.dto.PersonWithSignificantControlDto;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.ADDRESS_LINE1_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.ADDRESS_LINE2_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.APPOINTMENT_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.COUNTRY_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.DATE_EFFECTIVE_FROM;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.DATE_OF_BIRTH;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.FORENAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.FORMER_NAMES;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.GOVERNING_LAW;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.LEGAL_ENTITY_NAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.LEGAL_ENTITY_REGISTER_NAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.LEGAL_FORM;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.LOCALITY_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.NATIONALITY1;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.NATIONALITY2;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.NATURES_OF_CONTROL_LIST_DESCRIPTIONS;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.POA_PREFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.POSTAL_CODE_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.PREMISES_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.REGION_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.REGISTERED_COMPANY_NUMBER;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.RESIGNATION_DATE;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.SERVICE_PREFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.SURNAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PersonWithSignificantControlBuilder.URA_PREFIX;
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

    @Nested
    class IndividualPersonMapperTests {
        @Test
        void givenDao_whenMapsToDto_thenCorrect() {
            // given
            PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().individualPersonDao();

            // when
            PersonWithSignificantControlDto result = MAPPER.daoToDto(personWithSignificantControlDao);
            PersonWithSignificantControlDataDto dataDto = result.getData();

            // Grouped assertions for main fields
            assertThat(dataDto)
                    .extracting(
                            FN_APPOINTMENT_ID,
                            FN_DATE_EFFECTIVE_FROM,
                            FN_DATE_OF_BIRTH,
                            FN_FORENAME,
                            FN_FORMER_NAMES,
                            FN_KIND,
                            FN_NATIONALITY1,
                            FN_NATIONALITY2,
                            FN_RESIGNATION_DATE,
                            FN_SURNAME,
                            FN_TYPE
                    )
                    .containsExactly(
                            APPOINTMENT_ID,
                            DATE_EFFECTIVE_FROM,
                            DATE_OF_BIRTH,
                            FORENAME,
                            FORMER_NAMES,
                            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                            Nationality.BRITISH.getDescription(),
                            Nationality.FRENCH.getDescription(),
                            RESIGNATION_DATE,
                            SURNAME,
                            PersonWithSignificantControlType.INDIVIDUAL_PERSON
                    );

            // Assert naturesOfControl
            assertThat(dataDto.getNaturesOfControl())
                    .hasSize(3)
                    .containsExactlyInAnyOrder(NatureOfControl.RLE, NatureOfControl.INDIVIDUAL_FIRM_CONTROL, NatureOfControl.ORP_TRUST_CONTROL);

            assertAddress(dataDto.getServiceAddress(), SERVICE_PREFIX);
            assertAddress(dataDto.getUsualResidentialAddress(), URA_PREFIX);
        }

        @Test
        void givenDto_whenMapsToDao_thenCorrect() {
            // given
            PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlBuilder().individualPersonDto();

            // when
            PersonWithSignificantControlDao result = MAPPER.dtoToDao(personWithSignificantControlDto);
            PersonWithSignificantControlDataDao daoData = result.getData();

            // Grouped assertions for main fields
            assertThat(daoData)
                    .extracting(
                            FN_APPOINTMENT_ID,
                            FN_DATE_EFFECTIVE_FROM,
                            FN_DATE_OF_BIRTH,
                            FN_FORENAME,
                            FN_FORMER_NAMES,
                            FN_KIND,
                            FN_NATIONALITY1,
                            FN_NATIONALITY2,
                            FN_RESIGNATION_DATE,
                            FN_SURNAME,
                            FN_TYPE
                    )
                    .containsExactly(
                            APPOINTMENT_ID,
                            DATE_EFFECTIVE_FROM,
                            DATE_OF_BIRTH,
                            FORENAME,
                            FORMER_NAMES,
                            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                            NATIONALITY1,
                            NATIONALITY2,
                            RESIGNATION_DATE,
                            SURNAME,
                            PersonWithSignificantControlType.INDIVIDUAL_PERSON
                    );

            // Assert naturesOfControl
            assertThat(daoData.getNaturesOfControl())
                    .hasSize(3)
                    .allMatch(NATURES_OF_CONTROL_LIST_DESCRIPTIONS::contains);

            assertAddress(daoData.getServiceAddress(), SERVICE_PREFIX);
            assertAddress(daoData.getUsualResidentialAddress(), URA_PREFIX);
        }
    }

    @Nested
    class RelevantLegalEntityMapperTests {
        @Test
        void givenDao_whenMapsToDto_thenCorrect() {
            // given
            PersonWithSignificantControlDao personWithSignificantControlDao = new PersonWithSignificantControlBuilder().relevantLegalEntityDao();

            // when
            PersonWithSignificantControlDto result = MAPPER.daoToDto(personWithSignificantControlDao);
            PersonWithSignificantControlDataDto dataDto = result.getData();

            // Grouped assertions for main fields
            assertThat(dataDto)
                    .extracting(
                            FN_APPOINTMENT_ID,
                            FN_COUNTRY,
                            FN_DATE_EFFECTIVE_FROM,
                            FN_GOVERNING_LAW,
                            FN_KIND,
                            FN_LEGAL_ENTITY_NAME,
                            FN_LEGAL_ENTITY_REGISTER_NAME,
                            FN_LEGAL_ENTITY_REGISTRATION_LOCATION,
                            FN_LEGAL_FORM,
                            FN_REGISTERED_COMPANY_NUMBER,
                            FN_RESIGNATION_DATE,
                            FN_TYPE
                    )
                    .containsExactly(
                            APPOINTMENT_ID,
                            ENGLAND,
                            DATE_EFFECTIVE_FROM,
                            GOVERNING_LAW,
                            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                            LEGAL_ENTITY_NAME,
                            LEGAL_ENTITY_REGISTER_NAME,
                            ENGLAND,
                            LEGAL_FORM,
                            REGISTERED_COMPANY_NUMBER,
                            RESIGNATION_DATE,
                            PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY
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
            PersonWithSignificantControlDto personWithSignificantControlDto = new PersonWithSignificantControlBuilder().relevantLegalEntityDto();

            // when
            PersonWithSignificantControlDao result = MAPPER.dtoToDao(personWithSignificantControlDto);
            PersonWithSignificantControlDataDao daoData = result.getData();

            // Grouped assertions for main fields
            assertThat(daoData)
                    .extracting(
                            FN_APPOINTMENT_ID,
                            FN_COUNTRY,
                            FN_DATE_EFFECTIVE_FROM,
                            FN_GOVERNING_LAW,
                            FN_KIND,
                            FN_LEGAL_ENTITY_NAME,
                            FN_LEGAL_ENTITY_REGISTER_NAME,
                            FN_LEGAL_ENTITY_REGISTRATION_LOCATION,
                            FN_LEGAL_FORM,
                            FN_REGISTERED_COMPANY_NUMBER,
                            FN_RESIGNATION_DATE,
                            FN_TYPE
                    )
                    .containsExactly(
                            APPOINTMENT_ID,
                            ENGLAND.getDescription(),
                            DATE_EFFECTIVE_FROM,
                            GOVERNING_LAW,
                            FILING_KIND_PERSON_WITH_SIGNIFICANT_CONTROL,
                            LEGAL_ENTITY_NAME,
                            LEGAL_ENTITY_REGISTER_NAME,
                            ENGLAND.getDescription(),
                            LEGAL_FORM,
                            REGISTERED_COMPANY_NUMBER,
                            RESIGNATION_DATE,
                            PersonWithSignificantControlType.RELEVANT_LEGAL_ENTITY
                    );

            // Assert naturesOfControl
            assertThat(daoData.getNaturesOfControl())
                    .hasSize(3)
                    .allMatch(NATURES_OF_CONTROL_LIST_DESCRIPTIONS::contains);

            assertAddress(daoData.getPrincipalOfficeAddress(), POA_PREFIX);
            assertAddress(daoData.getServiceAddress(), SERVICE_PREFIX);
        }
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
