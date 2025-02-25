package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GeneralPartnerController.class})
@WebMvcTest(controllers = {GeneralPartnerController.class})
class GeneralPartnerControllerValidationTest {

    static String postUrl = "/transactions/863851-951242-143528/limited-partnership/general-partner";
    private static final String JSON_CORRECT = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_WITH_BELOW_MIN_FORENAME = """
             {
                "data": {
                  "forename": "",
                  "former_names": "",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";
    private static final String JSON_WITH_ABOVE_MAX_FORENAME = """
            {
                "data": {
                  "forename": "The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog             The quick brown fox jumps over the lazy dog",
                  "former_names": "",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";
    private static final String JSON_WITH_BELOW_MIN_SURNAME = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "",
                  "surname": "",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";
    private static final String JSON_WITH_ABOVE_MAX_SURNAME = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "",
                  "surname": "The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";
    private static final String JSON_WITH_ABOVE_MAX_FORMERNAME = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog The quick brown fox jumps over the lazy dog",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_INVALID_FORENAME = """
            {
                "data": {
                  "forename": "Жoe",
                  "former_names": "",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_INVALID_SURNAME = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "",
                  "surname": "BloГГs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_INVALID_FORMERNAME = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "ВЛАД",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": null,
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_INVALID_NATIONALITY = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "ВЛАД",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "ABSURDISTANI",
                  "nationality2": "",
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_FIRST_AND_SECOND_NATIONALITY_NOT_DIFFERENT = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "ВЛАД",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": "BRITISH",
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";

    private static final String JSON_INVALID_SECOND_NATIONALITY = """
            {
                "data": {
                  "forename": "Joe",
                  "former_names": "ВЛАД",
                  "surname": "Bloggs",
                  "date_of_birth": "2001-01-01",
                  "nationality1": "BRITISH",
                  "nationality2": "Absurdistani",
                  "kind": "kind",
                  "etag": "tag"
                }
            }""";



    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    GeneralPartnerController generalPartnerController;

    @MockBean
    GeneralPartnerService generalPartnerService;

    @MockBean
    private TransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();

        this.mockMvc = MockMvcBuilders.standaloneSetup(generalPartnerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn201()  throws Exception {
        String body = JSON_CORRECT;
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.postUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body)).andDo(
                                print()
                )
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            JSON_WITH_BELOW_MIN_FORENAME,
            JSON_WITH_ABOVE_MAX_FORENAME,
            JSON_WITH_BELOW_MIN_SURNAME,
            JSON_WITH_ABOVE_MAX_SURNAME,
            JSON_WITH_ABOVE_MAX_FORMERNAME,
            JSON_INVALID_FORENAME,
            JSON_INVALID_SURNAME,
            JSON_INVALID_FORMERNAME,
            JSON_INVALID_NATIONALITY,
            JSON_FIRST_AND_SECOND_NATIONALITY_NOT_DIFFERENT,
            JSON_INVALID_SECOND_NATIONALITY
    })
    void shouldReturn400(String body)  throws Exception {
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.postUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
