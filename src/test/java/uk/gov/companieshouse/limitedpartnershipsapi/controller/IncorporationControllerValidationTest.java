package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.config.InterceptorConfig;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;

@SpringBootTest
@AutoConfigureMockMvc
class IncorporationControllerValidationTest {

    private static final String INCORPORATION_ID = "123";
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    private static final String BASE_URL = "/transactions/" + TRANSACTION_ID + "/incorporation/limited-partnership";
    private static final String VALIDATE_STATUS_URL = BASE_URL + "/" + INCORPORATION_ID + "/validation-status";

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
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CostsService costsService;

    @MockitoBean
    private GeneralPartnerRepository generalPartnerRepository;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private LimitedPartnershipRepository limitedPartnershipRepository;

    @MockitoBean
    private LimitedPartnershipIncorporationRepository limitedPartnershipIncorporationRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private TransactionUtils transactionUtils;

    @MockitoBean
    private InterceptorConfig interceptorConfig;


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
        mockMvc.perform(post(IncorporationControllerValidationTest.BASE_URL)
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
        when(limitedPartnershipIncorporationRepository.insert((LimitedPartnershipIncorporationDao) any())).thenReturn(new LimitedPartnershipIncorporationDao());

        mockMvc.perform(post(IncorporationControllerValidationTest.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr(TRANSACTION_KEY, transaction)
                        .content(CORRECT_JSON))
                .andExpect(status().isCreated());
    }

    @Nested
    class ValidateIncorporation {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {
            when(transactionUtils.doesTransactionHaveALimitedPartnership(any())).thenReturn(true);
            when(limitedPartnershipRepository.findByTransactionId(any())).thenReturn(List.of(new LimitedPartnershipBuilder().withAddresses().buildDao()));
            when(generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(any())).thenReturn(List.of(new GeneralPartnerBuilder().personDao()));
            when(limitedPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(any())).thenReturn(List.of(new LimitedPartnerBuilder().personDao()));

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
        void shouldReturn200AndErrorDetailsIfDataErrors() throws Exception {
            GeneralPartnerDao generalPartnerDao = new GeneralPartnerBuilder().personDao();
            generalPartnerDao.getData().setForename("");
            generalPartnerDao.getData().setNationality1("UNKNOWN");

            when(transactionUtils.doesTransactionHaveALimitedPartnership(any())).thenReturn(true);
            when(limitedPartnershipRepository.findByTransactionId(any())).thenReturn(List.of(new LimitedPartnershipBuilder().withAddresses().buildDao()));
            when(generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(any())).thenReturn(List.of(generalPartnerDao));
            when(limitedPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(any())).thenReturn(List.of(new LimitedPartnerBuilder().personDao()));

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
        void shouldReturn200AndErrorDetailsIfInsufficientNumberOfPartners() throws Exception {
            when(transactionUtils.doesTransactionHaveALimitedPartnership(any())).thenReturn(true);
            when(limitedPartnershipRepository.findByTransactionId(any())).thenReturn(List.of(new LimitedPartnershipBuilder().withAddresses().buildDao()));
            when(generalPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(any())).thenReturn(Collections.emptyList());
            when(limitedPartnerRepository.findAllByTransactionIdOrderByUpdatedAtDesc(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("is_valid").value("false"))
                    .andExpect(jsonPath("$.['errors']").value(containsInAnyOrder(
                            allOf(hasEntry("location", "general_partners"), hasEntry("error", "At least one general partner is required")),
                            allOf(hasEntry("location", "limited_partners"), hasEntry("error", "At least one limited partner is required"))
                    )));
        }

        @Test
        void shouldReturn404IfLimitedPartnershipNotFound() throws Exception {
            mockMvc.perform(get(VALIDATE_STATUS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(""))
                    .andExpect(status().isNotFound());
        }
    }
}
