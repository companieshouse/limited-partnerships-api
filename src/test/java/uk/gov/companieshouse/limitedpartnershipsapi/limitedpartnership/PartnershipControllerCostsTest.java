package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipController;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CostsService;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        PartnershipController.class,
        GlobalExceptionHandler.class})
@WebMvcTest(controllers = PartnershipController.class)
class PartnershipControllerCostsTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String COSTS_URL = "/transactions/" + TRANSACTION_ID
            + "/limited-partnership/partnership/" + SUBMISSION_ID + "/costs";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PartnershipService partnershipService;

    @MockitoBean
    private CostsService costsService;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", REQUEST_ID);
        httpHeaders.add("ERIC-Identity", USER_ID);

        transaction = new TransactionBuilder().build();
    }

    @Test
    void shouldGetCosts() throws Exception {
        Cost cost = new Cost();
        cost.setDescription("Register a limited partnership");

        when(costsService.getPostTransitionLimitedPartnershipCost(transaction)).thenReturn(cost);

        getRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Register a limited partnership"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenGetCostsFails() throws Exception {
        when(costsService.getPostTransitionLimitedPartnershipCost(transaction)).thenThrow(new ServiceException("TEST"));

        getRequest()
                .andExpect(status().isInternalServerError());
    }

    private ResultActions getRequest() throws Exception {
        return mockMvc.perform(get(COSTS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .headers(httpHeaders)
                .requestAttr("transaction", transaction));
    }
}

