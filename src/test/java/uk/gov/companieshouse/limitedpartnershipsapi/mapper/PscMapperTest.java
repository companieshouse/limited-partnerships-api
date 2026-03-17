package uk.gov.companieshouse.limitedpartnershipsapi.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.Nationality;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.NatureOfControl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.ADDRESS_LINE1_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.ADDRESS_LINE2_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.APPOINTMENT_ID;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.COUNTRY_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.DATE_EFFECTIVE_FROM;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.DATE_OF_BIRTH;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.FORENAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.FORMER_NAMES;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.GOVERNING_LAW;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.LEGAL_ENTITY_NAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.LEGAL_ENTITY_REGISTER_NAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.LEGAL_FORM;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.LEGAL_PERSONALITY_STATEMENT_CHECKED;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.LOCALITY_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.NATIONALITY1;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.NATIONALITY2;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.NATURES_OF_CONTROL_LIST_DESCRIPTIONS;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.POA_PREFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.POSTAL_CODE_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.PREMISES_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.REGION_SUFFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.REGISTERED_COMPANY_NUMBER;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.RESIGNATION_DATE;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.SERVICE_PREFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.SURNAME;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder.URA_PREFIX;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.Country.ENGLAND;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PSC;


class PscMapperTest {
    private static final PscMapper MAPPER = Mappers.getMapper(PscMapper.class);

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
        PscDao pscDao = PscBuilder.getPscDao();

        // when
        PscDto result = MAPPER.daoToDto(pscDao);
        PscDataDto dataDto = result.getData();

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
                FN_LEGAL_PERSONALITY_STATEMENT_CHECKED,
                FN_NATIONALITY1,
                FN_NATIONALITY2,
                FN_REGISTERED_COMPANY_NUMBER,
                FN_RESIGNATION_DATE,
                FN_SURNAME
            )
            .containsExactly(
                APPOINTMENT_ID,
                ENGLAND,
                DATE_EFFECTIVE_FROM,
                DATE_OF_BIRTH,
                FORENAME,
                FORMER_NAMES,
                GOVERNING_LAW,
                FILING_KIND_PSC,
                LEGAL_ENTITY_NAME,
                LEGAL_ENTITY_REGISTER_NAME,
                ENGLAND,
                LEGAL_FORM,
                LEGAL_PERSONALITY_STATEMENT_CHECKED,
                Nationality.BRITISH,
                Nationality.FRENCH,
                REGISTERED_COMPANY_NUMBER,
                RESIGNATION_DATE,
                SURNAME
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
        PscDto pscDto = PscBuilder.getPscDto();

        // when
        PscDao result = MAPPER.dtoToDao(pscDto);
        PscDataDao daoData = result.getData();

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
                FN_LEGAL_PERSONALITY_STATEMENT_CHECKED,
                FN_NATIONALITY1,
                FN_NATIONALITY2,
                FN_REGISTERED_COMPANY_NUMBER,
                FN_RESIGNATION_DATE,
                FN_SURNAME
            )
            .containsExactly(
                APPOINTMENT_ID,
                ENGLAND.getDescription(),
                DATE_EFFECTIVE_FROM,
                DATE_OF_BIRTH,
                FORENAME,
                FORMER_NAMES,
                GOVERNING_LAW,
                FILING_KIND_PSC,
                LEGAL_ENTITY_NAME,
                LEGAL_ENTITY_REGISTER_NAME,
                ENGLAND.getDescription(),
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
            .allMatch(NATURES_OF_CONTROL_LIST_DESCRIPTIONS::contains);

        assertAddress(daoData.getPrincipalOfficeAddress(), POA_PREFIX);
        assertAddress(daoData.getServiceAddress(), SERVICE_PREFIX);
        assertAddress(daoData.getUsualResidentialAddress(), URA_PREFIX);
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
