package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.GeneralPartnerMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ContextConfiguration(classes = {GeneralPartnerController.class, GeneralPartnerService.class, GeneralPartnerValidator.class, GeneralPartnerMapperImpl.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {GeneralPartnerController.class})
class GeneralPartnerControllerValidationTest {

    private static final String GENERAL_PARTNER_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    private static final String BASE_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/general-partner";
    private static final String VALIDATE_STATUS_URL = BASE_URL + "/" + GENERAL_PARTNER_ID + "/validation-status";

    // PERSON
    private static final String JSON_CORRECT = """
            {
                "data": {
                  "forename": "Joe",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "not_disqualified_statement_checked": "true"
                }
            }""";

    private static final String JSON_WITH_BELOW_MIN_FORENAME = "{ \"data\": { \"forename\": \"\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";

    private static final String JSON_WITH_ABOVE_MAX_FORENAME = "{ \"data\": { \"forename\": \"The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";

    private static final String JSON_WITH_BELOW_MIN_SURNAME = "{ \"data\": { \"forename\": \"Joe\", \"surname\": \"\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";

    private static final String JSON_WITH_ABOVE_MAX_SURNAME = "{ \"data\": { \"forename\": \"Joe\", \"surname\": \"The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null} }";

    private static final String JSON_WITH_ABOVE_MAX_FORMER_NAMES = "{ \"data\": { \"forename\": \"Joe\", \"former_names\": \"The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null} }";

    private static final String JSON_INVALID_FORENAME = "{ \"data\": { \"forename\": \"Жoe\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";

    private static final String JSON_INVALID_SURNAME = "{ \"data\": { \"forename\": \"Joe\", \"surname\": \"BloГГs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";

    private static final String JSON_INVALID_FORMER_NAMES = "{ \"data\": { \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": null } }";

    private static final String JSON_INVALID_NATIONALITY = "{ \"data\": { \"forename\": \"Joe\", \"former_names\": \"ВЛАД\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"ABSURDISTANI\", \"nationality2\": null } }";

    private static final String JSON_INVALID_SECOND_NATIONALITY = "{ \"data\": { \"forename\": \"Joe\", \"surname\": \"Bloggs\", \"date_of_birth\": \"2001-01-01\", \"nationality1\": \"BRITISH\", \"nationality2\": \"Absurdistani\" } }";

    // LEGAL ENTITY
    private static final String JSON_GENERAL_LEGAL_ENTITY_CORRECT = """
            {
              "data": {
                "legal_entity_name": "My Company Name",
                "legal_form": "Form ABC",
                "governing_law": "Act of law",
                "legal_entity_register_name": "Register of somewhere",
                "legal_entity_registration_location": "Scotland",
                "registered_company_number": "12345678",
                "not_disqualified_statement_checked": true
              }
            }""";

    private static final String JSON_GENERAL_LEGAL_ENTITY_INVALID_COUNTRY = """
            {
              "data": {
                "legal_entity_name": "My Company Name",
                "legal_form": "Form ABC",
                "governing_law": "Act of law",
                "legal_entity_register_name": "Register of somewhere",
                "legal_entity_registration_location": "Wrong Country",
                "registered_company_number": "12345678",
                "not_disqualified_statement_checked": true
              }
            }""";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().forPartner(
            FILING_KIND_LIMITED_PARTNER,
            URL_GET_LIMITED_PARTNER,
            GENERAL_PARTNER_ID
    ).build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeneralPartnerRepository repository;

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
    @CsvSource(value = {
            JSON_WITH_BELOW_MIN_FORENAME + "$ data.forename $ Forename must be greater than 1",
            JSON_WITH_ABOVE_MAX_FORENAME + "$ data.forename $ Forename must be less than 50",
            JSON_WITH_BELOW_MIN_SURNAME + "$ data.surname $ Surname must be greater than 1",
            JSON_WITH_ABOVE_MAX_SURNAME + "$ data.surname $ Surname must be less than 160",
            JSON_WITH_ABOVE_MAX_FORMER_NAMES + "$ data.formerNames $ Former names must be less than 160",
            JSON_INVALID_FORENAME + "$ data.forename $ Forename " + INVALID_CHARACTERS_MESSAGE,
            JSON_INVALID_SURNAME + "$ data.surname $ Surname " + INVALID_CHARACTERS_MESSAGE,
            JSON_INVALID_FORMER_NAMES + "$ data.formerNames $ Former names " + INVALID_CHARACTERS_MESSAGE,
            JSON_INVALID_NATIONALITY + "$ data.nationality1 $ First nationality must be valid",
            JSON_INVALID_SECOND_NATIONALITY + "$ data.nationality2 $ Second nationality must be valid"
    }, delimiter = '$')
    void shouldReturn400(String body, String field, String errorMessage) throws Exception {
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
    }

    @Test
    void shouldReturn201() throws Exception {
        mocks();

        mockMvc.perform(post(GeneralPartnerControllerValidationTest.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(JSON_CORRECT))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn201WhenCreatingGeneralPartnerLegalEntity() throws Exception {
        mocks();

        mockMvc.perform(post(GeneralPartnerControllerValidationTest.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(JSON_GENERAL_LEGAL_ENTITY_CORRECT))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400WhenCreatingGeneralPartnerLegalEntityWithWrongCountry() throws Exception {
        mocks();

        mockMvc.perform(post(GeneralPartnerControllerValidationTest.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(JSON_GENERAL_LEGAL_ENTITY_INVALID_COUNTRY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['data.legalEntityRegistrationLocation']").value("Legal entity registration location must be valid"));
    }

    @Nested
    class ValidatePartner {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            mocks();

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("true"));
        }

        @Test
        void shouldReturn200AndErrorDetailsIfErrors() throws Exception {
            GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();
            generalPartnerDao.getData().setForename("");
            generalPartnerDao.getData().setNationality1("UNKNOWN");

            mocks(generalPartnerDao);

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("false"))
                    .andExpect(jsonPath("$.['errors']").value(containsInAnyOrder(
                            allOf(hasEntry("location", "data.forename"), hasEntry("error", "Forename must be greater than 1")),
                            allOf(hasEntry("location", "data.nationality1"), hasEntry("error", "First nationality must be valid"))
                    )));
        }

        @Test
        void shouldReturn404IfGeneralPartnerNotFound() throws Exception {
            when(repository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.empty());

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isNotFound());
        }
    }

    private void mocks(GeneralPartnerDao generalPartnerDao) {
        when(repository.insert((GeneralPartnerDao) any())).thenReturn(generalPartnerDao);
        when(repository.save(any())).thenReturn(generalPartnerDao);
        when(repository.findById(GENERAL_PARTNER_ID)).thenReturn(Optional.of(generalPartnerDao));

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);
    }

    private void mocks() {
        GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();

        mocks(generalPartnerDao);
    }
}
