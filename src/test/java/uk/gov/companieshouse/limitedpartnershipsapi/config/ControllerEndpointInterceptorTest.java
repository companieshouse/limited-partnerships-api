package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.controller.FilingsController;
import uk.gov.companieshouse.limitedpartnershipsapi.controller.GeneralPartnerController;
import uk.gov.companieshouse.limitedpartnershipsapi.controller.IncorporationController;
import uk.gov.companieshouse.limitedpartnershipsapi.controller.LimitedPartnerController;
import uk.gov.companieshouse.limitedpartnershipsapi.controller.PartnershipController;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.CustomUserAuthenticationInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.FilingsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipIncorporationService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = { IncorporationController.class, PartnershipController.class, FilingsController.class,
        GeneralPartnerController.class, LimitedPartnerController.class, CostsService.class,
        GlobalExceptionHandler.class, InterceptorConfig.class, LoggingInterceptor.class })
@WebMvcTest({ IncorporationController.class, PartnershipController.class, FilingsController.class,
        GeneralPartnerController.class, LimitedPartnerController.class })
class ControllerEndpointInterceptorTest {

    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String INCORPORATION_ID = TransactionBuilder.SUBMISSION_ID;
    private static final String PARTNERSHIP_ID = TransactionBuilder.SUBMISSION_ID;
    private static final String GENERAL_PARTNER_ID = GeneralPartnerBuilder.GENERAL_PARTNER_ID;
    private static final String LIMITED_PARTNER_ID = LimitedPartnerBuilder.LIMITED_PARTNER_ID;

    // Example valid CRUD URLs:
    private static final String GET_PARTNERSHIP_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/partnership/" + PARTNERSHIP_ID;
    private static final String CREATE_GENERAL_PARTNER_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/general-partner";
    private static final String UPDATE_LIMITED_PARTNER_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID;

    // Example valid Costs and Filings URLs:
    private static final String INCORPORATION_COSTS_URL = "/transactions/" + TRANSACTION_ID + "/incorporation/limited-partnership/" + INCORPORATION_ID + "/costs";
    private static final String FILINGS_URL = "/private/transactions/" + TRANSACTION_ID + "/incorporation/limited-partnership/" + INCORPORATION_ID + "/filings";

    private HttpHeaders httpHeaders;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnershipIncorporationService incorporationService;

    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;

    @MockitoBean
    private GeneralPartnerService generalPartnerService;

    @MockitoBean
    private LimitedPartnerService limitedPartnerService;

    @MockitoBean
    private FilingsService filingsService;

    @MockitoBean
    private CostsService costsService;

    @MockitoBean
    private LoggingInterceptor loggingInterceptor;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor;

    @MockitoBean
    private InternalUserInterceptor internalUserInterceptor;

    @BeforeEach
    void setUp() {
        when(loggingInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(customUserAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(transactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }

    @Test
    void testInterceptorCallsForGetPartnership() throws Exception {
        mockMvc.perform(get(GET_PARTNERSHIP_URL)
                        .headers(httpHeaders)
                        .requestAttr("transaction", new Transaction()))
                .andExpect(status().isOk());

        verify(customUserAuthenticationInterceptor, times(1)).preHandle(any(), any(), any());
        verify(transactionInterceptor, times(1)).preHandle(any(), any(), any());
        verify(internalUserInterceptor, times(0)).preHandle(any(), any(), any());
    }

    @Test
    void testInterceptorCallsForCreateGeneralPartner() throws Exception {
        mockMvc.perform(post(CREATE_GENERAL_PARTNER_URL)
                        .headers(httpHeaders)
                        .requestAttr("transaction", new Transaction())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content("{}")  // Payload content not important, as controller code is not entered
                )
                .andExpect(status().isCreated());

        verify(customUserAuthenticationInterceptor, times(1)).preHandle(any(), any(), any());
        verify(transactionInterceptor, times(1)).preHandle(any(), any(), any());
        verify(internalUserInterceptor, times(0)).preHandle(any(), any(), any());
    }

    @Test
    void testInterceptorCallsForUpdateLimitedPartner() throws Exception {
        mockMvc.perform(patch(UPDATE_LIMITED_PARTNER_URL)
                        .headers(httpHeaders)
                        .requestAttr("transaction", new Transaction())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content("{}")  // Payload content not important, as controller code is not entered
                )
                .andExpect(status().isOk());

        verify(customUserAuthenticationInterceptor, times(1)).preHandle(any(), any(), any());
        verify(transactionInterceptor, times(1)).preHandle(any(), any(), any());
        verify(internalUserInterceptor, times(0)).preHandle(any(), any(), any());
    }

    @Test
    void testInterceptorCallsForGettingIncorporationCosts() throws Exception {
        mockMvc.perform(get(INCORPORATION_COSTS_URL)
                        .headers(httpHeaders)
                        .requestAttr("transaction", new Transaction()))
                .andExpect(status().isOk());

        verify(customUserAuthenticationInterceptor, times(1)).preHandle(any(), any(), any());
        verify(transactionInterceptor, times(1)).preHandle(any(), any(), any());
        verify(internalUserInterceptor, times(1)).preHandle(any(), any(), any());
    }

    @Test
    void testInterceptorCallsForGettingFilings() throws Exception {
        mockMvc.perform(get(FILINGS_URL)
                        .headers(httpHeaders)
                        .requestAttr("transaction", new Transaction()))
                .andExpect(status().isOk());

        verify(customUserAuthenticationInterceptor, times(0)).preHandle(any(), any(), any());
        verify(transactionInterceptor, times(1)).preHandle(any(), any(), any());
        verify(internalUserInterceptor, times(1)).preHandle(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/Blah",

            // invalid CRUD URLs
            "/transactions/" + TRANSACTION_ID + "/limited-partnership/invalid-sub-path/partnership/" + PARTNERSHIP_ID,
            "/transactions/" + TRANSACTION_ID + "/limited-partnership/invalid-sub-path/general-partner/" + GENERAL_PARTNER_ID,
            "/transactions/" + TRANSACTION_ID + "/limited-partnership/invalid-sub-path/limited-partner/" + LIMITED_PARTNER_ID,

            // Invalid costs URLs
            "/transactions/" + TRANSACTION_ID + "/incorporation/invalid-sub-path/limited-partnership/costs",
            "/transactions/" + TRANSACTION_ID + "/invalid-sub-path/" + INCORPORATION_ID + "/costs",

            // Invalid filings URLs
            "/private/transactions/" + TRANSACTION_ID + "/limited-partnership/general-partner/" + GENERAL_PARTNER_ID + "/filings",
            "/private/transactions/" + TRANSACTION_ID + "/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID + "/filings"
        })
    void testNoInterceptorsAreCalledForInvalidUrls(String url) throws Exception {
        mockMvc.perform(get(url)
                        .headers(httpHeaders)
                        .requestAttr("transaction", new Transaction()))
                .andExpect(status().isInternalServerError());

        verify(customUserAuthenticationInterceptor, times(0)).preHandle(any(), any(), any());
        verify(transactionInterceptor, times(0)).preHandle(any(), any(), any());
        verify(internalUserInterceptor, times(0)).preHandle(any(), any(), any());
    }
}
