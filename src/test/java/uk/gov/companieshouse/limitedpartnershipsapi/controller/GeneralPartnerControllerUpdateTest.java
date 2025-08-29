package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.CompanyBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@ContextConfiguration(classes = {GeneralPartnerController.class, GeneralPartnerService.class, GeneralPartnerValidator.class, GeneralPartnerMapperImpl.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {GeneralPartnerController.class})
class GeneralPartnerControllerUpdateTest {

    private static final String JSON_GENERAL_PARTNER_PERSON = """
            {
              "forename": "Joe",
              "surname": "Bloggs",
              "date_of_birth": "2001-01-01",
              "nationality1": "BRITISH",
              "nationality2": null
            }""";

    private static final String JSON_GENERAL_LEGAL_ENTITY = """
            {
                "legal_entity_name": "My Company ltd",
                "legal_form": "Limited Company",
                "governing_law": "Act of law",
                "legal_entity_register_name": "US Register",
                "legal_entity_registration_location": "United States",
                "registered_company_number": "12345678",
                "not_disqualified_statement_checked": true
            }""";

    private static final String JSON_WITH_BELOW_MIN_FORENAME = "{ \"forename\": \"\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

    private static final String JSON_WITH_ABOVE_MAX_SURNAME = "{ \"forename\": \"Joe\", \"surname\": \"The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

    private static final String JSON_INVALID_FORMER_NAMES = "{ \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null }";

    private static final String JSON_INVALID_NATIONALITY = "{ \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"ABSURDISTANI\", \"nationality2\": null }";

    private static final String TRANSACTION_ID = "863851-951242-143528";
    private static final String GENERAL_PARTNER_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String GENERAL_PARTNER_LIST_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/general-partners";
    private static final String GENERAL_PARTNER_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/general-partner/" + GENERAL_PARTNER_ID;
    private static final String GENERAL_PARTNER_POST_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/general-partner";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_GENERAL_PARTNER,
            URL_GET_GENERAL_PARTNER,
            GENERAL_PARTNER_ID
    ).build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeneralPartnerRepository generalPartnerRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            JSON_GENERAL_PARTNER_PERSON,
            JSON_GENERAL_LEGAL_ENTITY
    })
    void shouldReturn200(String body) throws Exception {
        mocks();

        mockMvc.perform(patch(GENERAL_PARTNER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(value = {
            JSON_WITH_BELOW_MIN_FORENAME + "$ data.forename $ Forename must be greater than 1",
            JSON_WITH_ABOVE_MAX_SURNAME + "$ data.surname $ Surname must be less than 160",
            JSON_INVALID_FORMER_NAMES + "$ data.formerNames $ Former names " + INVALID_CHARACTERS_MESSAGE,
            JSON_INVALID_NATIONALITY + "$ data.nationality1 $ First nationality must be valid"
    }, delimiter = '$')
    void shouldReturn400(String body, String field, String errorMessage) throws Exception {
        mocks();

        mockMvc.perform(patch(GENERAL_PARTNER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
    }

    @Nested
    class CreatePartnerWithDateEffectiveFrom {
        private static final String JSON_GENERAL_PARTNER_PERSON = """
                {"data": {
                      "forename": "Joe",
                      "surname": "Bloggs",
                      "date_of_birth": "2001-01-01",
                      "nationality1": "BRITISH",
                      "nationality2": null,
                      "date_effective_from": "2023-10-01"
                    }
                }""";

        private static final String JSON_GENERAL_LEGAL_ENTITY = """
                {"data": {
                        "legal_entity_name": "My Company ltd",
                        "legal_form": "Limited Company",
                        "governing_law": "Act of law",
                        "legal_entity_register_name": "US Register",
                        "legal_entity_registration_location": "United States",
                        "registered_company_number": "12345678",
                        "not_disqualified_statement_checked": true,
                        "date_effective_from": "2023-12-01"
                    }
                }""";

        private static final String JSON_PATCH_GENERAL_PARTNER_PERSON = "{ \"forename\": \"Joe\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null, \"date_effective_from\": \"2023-10-01\" }";

        private static final String JSON_PATCH_GENERAL_LEGAL_ENTITY = """
                {
                    "legal_entity_name": "My Company ltd",
                    "legal_form": "Limited Company",
                    "governing_law": "Act of law",
                    "legal_entity_register_name": "US Register",
                    "legal_entity_registration_location": "United States",
                    "registered_company_number": "12345678",
                    "not_disqualified_statement_checked": true,
                    "date_effective_from": "2023-12-01"
                }""";

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_GENERAL_PARTNER_PERSON,
                JSON_GENERAL_LEGAL_ENTITY
        })
        void shouldReturn201(String body) throws Exception {
            mocks();

            CompanyProfileApi companyProfile = new CompanyBuilder().build();
            when(companyService.getCompanyProfile(any())).thenReturn(companyProfile);

            transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

            mockMvc.perform(post(GENERAL_PARTNER_POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_PATCH_GENERAL_PARTNER_PERSON,
                JSON_PATCH_GENERAL_LEGAL_ENTITY
        })
        void shouldReturn200(String body) throws Exception {
            mocks();

            CompanyProfileApi companyProfile = new CompanyBuilder().build();
            when(companyService.getCompanyProfile(any())).thenReturn(companyProfile);

            transaction.setFilingMode(IncorporationKind.TRANSITION.getDescription());

            mockMvc.perform(patch(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        private static final String JSON_PERSON_WITHOUT_DATE_EFFECTIVE_FROM = "{\"data\": { \"forename\": \"Joe\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";
        private static final String JSON_LEGAL_ENTITY_WITHOUT_DATE_EFFECTIVE_FROM = "{\"data\": { \"legal_entity_name\": \"My Company ltd\", \"legal_form\": \"Limited Company\", \"governing_law\": \"Act of law\", \"legal_entity_register_name\": \"US Register\", \"legal_entity_registration_location\": \"United States\", \"registered_company_number\": \"12345678\", \"not_disqualified_statement_checked\": true } }";
        private static final String JSON_PERSON_DATE_EFFECTIVE_FROM_BEFORE_CREATION = "{\"data\": { \"forename\": \"Joe\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null, \"date_effective_from\": \"2020-10-01\" } }";
        private static final String JSON_LEGAL_ENTITY_DATE_EFFECTIVE_FROM_BEFORE_CREATION = "{\"data\": { \"legal_entity_name\": \"My Company ltd\", \"legal_form\": \"Limited Company\", \"governing_law\": \"Act of law\", \"legal_entity_register_name\": \"US Register\", \"legal_entity_registration_location\": \"United States\", \"registered_company_number\": \"12345678\", \"not_disqualified_statement_checked\": true, \"date_effective_from\": \"2020-10-01\" } }";
        private static final String JSON_LEGAL_ENTITY_DATE_EFFECTIVE_FROM_IN_FUTURE = "{\"data\": { \"legal_entity_name\": \"My Company ltd\", \"legal_form\": \"Limited Company\", \"governing_law\": \"Act of law\", \"legal_entity_register_name\": \"US Register\", \"legal_entity_registration_location\": \"United States\", \"registered_company_number\": \"12345678\", \"not_disqualified_statement_checked\": true, \"date_effective_from\": \"2030-10-01\" } }";

        private static final String TRANSITION_KIND = "limited-partnership-transition";
        private static final String POST_TRANSITION_KIND = "limited-partnership-post-transition";

        @ParameterizedTest
        @CsvSource(value = {
                JSON_PERSON_WITHOUT_DATE_EFFECTIVE_FROM + "$ data.dateEffectiveFrom $ Partner date effective from is required $" + TRANSITION_KIND,
                JSON_LEGAL_ENTITY_WITHOUT_DATE_EFFECTIVE_FROM + "$ data.dateEffectiveFrom $ Partner date effective from is required $" + TRANSITION_KIND,
                JSON_PERSON_DATE_EFFECTIVE_FROM_BEFORE_CREATION + "$ data.dateEffectiveFrom $ Partner date effective from cannot be before the incorporation date $" + TRANSITION_KIND,
                JSON_LEGAL_ENTITY_DATE_EFFECTIVE_FROM_BEFORE_CREATION + "$ data.dateEffectiveFrom $ Partner date effective from cannot be before the incorporation date $" + TRANSITION_KIND,
                JSON_LEGAL_ENTITY_DATE_EFFECTIVE_FROM_IN_FUTURE + "$ data.dateEffectiveFrom $ Partner date effective from must be in the past $" + TRANSITION_KIND,
                JSON_PERSON_WITHOUT_DATE_EFFECTIVE_FROM + "$ data.dateEffectiveFrom $ Partner date effective from is required $" + POST_TRANSITION_KIND,
                JSON_LEGAL_ENTITY_WITHOUT_DATE_EFFECTIVE_FROM + "$ data.dateEffectiveFrom $ Partner date effective from is required $" + POST_TRANSITION_KIND,
                JSON_PERSON_DATE_EFFECTIVE_FROM_BEFORE_CREATION + "$ data.dateEffectiveFrom $ Partner date effective from cannot be before the incorporation date $" + POST_TRANSITION_KIND,
                JSON_LEGAL_ENTITY_DATE_EFFECTIVE_FROM_BEFORE_CREATION + "$ data.dateEffectiveFrom $ Partner date effective from cannot be before the incorporation date $" + POST_TRANSITION_KIND,
                JSON_LEGAL_ENTITY_DATE_EFFECTIVE_FROM_IN_FUTURE + "$ data.dateEffectiveFrom $ Partner date effective from must be in the past $" + POST_TRANSITION_KIND
        }, delimiter = '$')
        void shouldReturn400(String body, String field, String errorMessage, String filingMode) throws Exception {
            mocks();

            CompanyProfileApi companyProfile = new CompanyBuilder().build();
            when(companyService.getCompanyProfile(any())).thenReturn(companyProfile);

            transaction.setFilingMode(filingMode);

            mockMvc.perform(post(GENERAL_PARTNER_POST_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
        }
    }

    @Nested
    class Addresses {
        // correct addresses
        private static final String JSON_POA_UK = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_NOT_UK = "{\"principal_office_address\":{\"postal_code\":\"12345\",\"premises\":\"2\",\"address_line_1\":\"test rue\",\"address_line_2\":\"\",\"locality\":\"TOULOUSE\",\"country\":\"France\"}}";
        private static final String JSON_POA_NOT_UK_WITHOUT_POSTAL_CODE = "{\"principal_office_address\":{\"premises\":\"2\",\"address_line_1\":\"test rue\",\"address_line_2\":\"\",\"locality\":\"TOULOUSE\",\"country\":\"France\"}}";
        private static final String JSON_POA_POSTCODE_NOT_UK_MAINLAND_WITHOUT_UK_COUNTRY = "{\"principal_office_address\":{\"postal_code\":\"JE2 3AA\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"Jersey\"}}";

        // principal office address
        private static final String JSON_POA_POSTCODE_EMPTY = "{\"principal_office_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_POSTCODE_NOT_CORRECT = "{\"principal_office_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_POSTCODE_NOT_UK_MAINLAND_WITH_UK_COUNTRY = "{\"principal_office_address\":{\"postal_code\":\"JE2 3AA\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_ADDRESS_LINE_1_TOO_SHORT = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

        private static final String JSON_POA_MISSING_POSTCODE = "{\"principal_office_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_MISSING_PREMISES = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_MISSING_ADDRESS_LINE_1 = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_POA_MISSING_LOCALITY = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
        private static final String JSON_POA_MISSING_COUNTRY = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

        // service address
        private static final String JSON_SA_POSTCODE_EMPTY = "{\"service_address\":{\"postal_code\":\"\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_POSTCODE_NOT_CORRECT = "{\"service_address\":{\"postal_code\":\"1ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_ADDRESS_LINE_1_TOO_SHORT = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

        private static final String JSON_SA_MISSING_POSTCODE = "{\"service_address\":{\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_PREMISES = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_ADDRESS_LINE_1 = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_LOCALITY = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"country\":\"England\"}}";
        private static final String JSON_SA_MISSING_COUNTRY = "{\"service_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"DUNCALF STREET\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\"}}";

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_POA_UK,
                JSON_POA_NOT_UK,
                JSON_POA_NOT_UK_WITHOUT_POSTAL_CODE,
                JSON_POA_POSTCODE_NOT_UK_MAINLAND_WITHOUT_UK_COUNTRY
        })
        void shouldReturn200(String body) throws Exception {
            mocks();

            mockMvc.perform(patch(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturn400IfAddressLine1IsTooLong() throws Exception {
            mocks();

            String longAddressLine1 = StringUtils.repeat("A", 51);
            String body = "{\"principal_office_address\":{\"postal_code\":\"ST6 3LJ\",\"premises\":\"2\",\"address_line_1\":\"" + longAddressLine1 + "\",\"address_line_2\":\"\",\"locality\":\"STOKE-ON-TRENT\",\"country\":\"England\"}}";

            mockMvc.perform(patch(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @CsvSource(value = {
                JSON_POA_POSTCODE_EMPTY + "$ data.principalOfficeAddress.postalCode $ Postcode must not be null",
                JSON_POA_POSTCODE_NOT_CORRECT + "$ data.principalOfficeAddress.postalCode $ Invalid postcode format",
                JSON_POA_POSTCODE_NOT_UK_MAINLAND_WITH_UK_COUNTRY + "$ data.principalOfficeAddress.postalCode $ Must be UK mainland postcode",
                JSON_POA_ADDRESS_LINE_1_TOO_SHORT + "$ data.principalOfficeAddress.addressLine1 $ Address line 1 must be greater than 1",
                JSON_SA_POSTCODE_EMPTY + "$ data.serviceAddress.postalCode $ Postcode must not be null",
                JSON_SA_POSTCODE_NOT_CORRECT + "$ data.serviceAddress.postalCode $ Invalid postcode format",
                JSON_SA_ADDRESS_LINE_1_TOO_SHORT + "$ data.serviceAddress.addressLine1 $ Address line 1 must be greater than 1"
        }, delimiter = '$')
        void shouldReturn400IfFieldIncorrect(String body, String field, String errorMessage) throws Exception {
            mocks();

            mockMvc.perform(patch(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
        }

        @ParameterizedTest
        @CsvSource(value = {
                JSON_POA_MISSING_POSTCODE + "$ data.principalOfficeAddress.postalCode $ Postcode must not be null",
                JSON_POA_MISSING_PREMISES + "$ data.principalOfficeAddress.premises $ Property name or number must not be null",
                JSON_POA_MISSING_ADDRESS_LINE_1 + "$ data.principalOfficeAddress.addressLine1 $ Address line 1 must not be null",
                JSON_POA_MISSING_LOCALITY + "$ data.principalOfficeAddress.locality $ Town or city must not be null",
                JSON_POA_MISSING_COUNTRY + "$ data.principalOfficeAddress.country $ Country must not be null",
                JSON_SA_MISSING_POSTCODE + "$ data.serviceAddress.postalCode $ Postcode must not be null",
                JSON_SA_MISSING_PREMISES + "$ data.serviceAddress.premises $ Property name or number must not be null",
                JSON_SA_MISSING_ADDRESS_LINE_1 + "$ data.serviceAddress.addressLine1 $ Address line 1 must not be null",
                JSON_SA_MISSING_LOCALITY + "$ data.serviceAddress.locality $ Town or city must not be null",
                JSON_SA_MISSING_COUNTRY + "$ data.serviceAddress.country $ Country must not be null"
        }, delimiter = '$')
        void shouldReturn400IfRequiredFieldIsMissing(String body, String field, String errorMessage) throws Exception {
            mocks();

            mockMvc.perform(patch(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
        }
    }

    @Nested
    class DeleteGeneralPartner {
        @Test
        void shouldReturn204() throws Exception {
            mocks();

            mockMvc.perform(delete(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404() throws Exception {
            when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.empty());

            mockMvc.perform(delete(GENERAL_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void shouldReturnTheListOfGPWithTheCompletedField() throws Exception {
        GeneralPartnerDao generalPartnerDao1 = new GeneralPartnerBuilder().personDao();
        generalPartnerDao1.setTransactionId(TRANSACTION_ID);

        GeneralPartnerDao generalPartnerDao2 = new GeneralPartnerBuilder().personDao();
        generalPartnerDao2.setTransactionId(TRANSACTION_ID);
        generalPartnerDao2.getData().setUsualResidentialAddress(null);

        List<GeneralPartnerDao> generalPartners = List.of(generalPartnerDao1, generalPartnerDao2);

        when(generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(TRANSACTION_ID)).thenReturn(generalPartners);

        mockMvc.perform(get(GENERAL_PARTNER_LIST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].data.completed").value(true))
                .andExpect(jsonPath("$.[1].data.completed").value(false));
    }

    private void mocks(GeneralPartnerDao generalPartnerDao) {
        when(generalPartnerRepository.insert((GeneralPartnerDao) any())).thenReturn(generalPartnerDao);
        when(generalPartnerRepository.save(any())).thenReturn(generalPartnerDao);
        when(generalPartnerRepository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);
    }

    private void mocks() {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

        mocks(generalPartnerDao);
    }
}
