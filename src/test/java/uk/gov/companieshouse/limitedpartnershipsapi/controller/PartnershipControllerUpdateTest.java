package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {PartnershipController.class, CostsService.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {PartnershipController.class})
public class PartnershipControllerUpdateTest {
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String PARTNERSHIP_ID = TransactionBuilder.SUBMISSION_ID;

    private static final String PARTNERSHIP_COSTS_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/partnership/" + PARTNERSHIP_ID + "/costs";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;

    @MockitoBean
    LimitedPartnershipIncorporationRepository repository;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }

    @Nested
    class Costs {

        @Test
        void shouldReturn200() throws Exception {
            mockMvc.perform(get(PARTNERSHIP_COSTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].amount").value("0.00"))
                    .andExpect(jsonPath("$.[0].description").value("Limited Partnership fee"));
        }
    }

}
