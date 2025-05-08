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
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.INVALID_CHARACTERS_MESSAGE;

@ContextConfiguration(classes = {GeneralPartnerController.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {GeneralPartnerController.class})
class GeneralPartnerControllerValidationTest {

    private static final String GENERAL_PARTNER_ID = "93702824-9062-4c63-a694-716acffccdd5";

    private static final String POST_URL = "/transactions/863851-951242-143528/limited-partnership/general-partner";
    private static final String VALIDATE_STATUS_URL = POST_URL + "/" + GENERAL_PARTNER_ID + "/validation-status";

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
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeneralPartnerService generalPartnerService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();
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
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.POST_URL)
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
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(JSON_CORRECT))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn201WhenCreatingGeneralPartnerLegalEntity() throws Exception {
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(JSON_GENERAL_LEGAL_ENTITY_CORRECT))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400WhenCreatingGeneralPartnerLegalEntityWithWrongCountry() throws Exception {
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.POST_URL)
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
            List<ValidationStatusError> errorsList = new ArrayList<>();
            errorsList.add(new ValidationStatusError("Forename must be greater than 1", "here", null, null));
            errorsList.add(new ValidationStatusError("First nationality must be valid", "there", null, null));
            when(generalPartnerService.validateGeneralPartner(transaction, GENERAL_PARTNER_ID)).thenReturn(errorsList);

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("false"))
                    .andExpect(jsonPath("$.['errors'][0].['location']").value("here"))
                    .andExpect(jsonPath("$.['errors'][0].['error']").value("Forename must be greater than 1"))
                    .andExpect(jsonPath("$.['errors'][1].['location']").value("there"))
                    .andExpect(jsonPath("$.['errors'][1].['error']").value("First nationality must be valid"));
        }

        @Test
        void shouldReturn404IfGeneralPartnerNotFound() throws Exception {
            when(generalPartnerService.validateGeneralPartner(transaction, GENERAL_PARTNER_ID))
                    .thenThrow(new ResourceNotFoundException("Error"));

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn500IfUnexpectedServiceExceptionThrown() throws Exception {
            when(generalPartnerService.validateGeneralPartner(transaction, GENERAL_PARTNER_ID))
                    .thenThrow(new ServiceException("Error"));

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isInternalServerError());
        }
    }
}
