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
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind.REGISTRATION;

@ContextConfiguration(classes = {IncorporationController.class, CostsService.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {IncorporationController.class})
public class IncorporationControllerUpdateTest {
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String INCORPORATION_ID = "09876543";

    private static final String INCORPORATION_COSTS_URL = "/transactions/" + TRANSACTION_ID + "/incorporation/limited-partnership/" + INCORPORATION_ID + "/costs";

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnershipIncorporationService limitedPartnershipIncorporationService;

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

            LimitedPartnershipIncorporationDao incorporationDao = new LimitedPartnershipIncorporationDao();
            incorporationDao.setId(INCORPORATION_ID);
            incorporationDao.getData().setKind(REGISTRATION.getDescription());

            when(repository.findById(INCORPORATION_ID)).thenReturn(Optional.of(incorporationDao));

            mockMvc.perform(get(INCORPORATION_COSTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].amount").value("100.00"))
                    .andExpect(jsonPath("$.[0].description").value("Register Limited Partnership fee"));
        }

        @Test
        void shouldReturn404() throws Exception {
            when(repository.findById(INCORPORATION_ID)).thenReturn(Optional.empty());

            mockMvc.perform(get(INCORPORATION_COSTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNotFound());
        }
    }

}
