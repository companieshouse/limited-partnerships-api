package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;

@ContextConfiguration(classes = {IncorporationController.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {IncorporationController.class})
class IncorporationControllerValidationTest {

    private static final String POST_URL = "/transactions/863851-951242-143528/incorporation/limited-partnership";

    private static final String CORRECT_JSON = """
            {
                "data": {
                  "kind": "limited-partnership-registration"
                }
            }""";

    private static final String INCORRECT_JSON_WITH_MISSING_KIND = "{ \"data\": { \"forename\": \"Joe\" } }";

    private static final String INCORRECT_JSON_WITH_INVALID_KIND = "{ \"data\": { \"kind\": \"something-dodgy\" } }";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnershipIncorporationService limitedPartnershipIncorporationService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CostsService costsService;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }

    @ParameterizedTest
    @CsvSource(value = {
            INCORRECT_JSON_WITH_MISSING_KIND + "$ data.kind $ Kind must not be null",
            INCORRECT_JSON_WITH_INVALID_KIND + "$ data.kind $ Kind must be valid"
    }, delimiter = '$')
    void shouldReturn400(String body, String field, String errorMessage) throws Exception {
        mockMvc.perform(post(IncorporationControllerValidationTest.POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr(TRANSACTION_KEY, transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['" + field + "']").value(errorMessage));
    }

    @Test
    void shouldReturn201() throws Exception {
        mockMvc.perform(post(IncorporationControllerValidationTest.POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr(TRANSACTION_KEY, transaction)
                        .content(CORRECT_JSON))
                .andExpect(status().isCreated());
    }
}
