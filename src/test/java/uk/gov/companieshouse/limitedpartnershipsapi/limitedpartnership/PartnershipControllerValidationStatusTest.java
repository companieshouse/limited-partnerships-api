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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipController;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CostsService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        PartnershipController.class,
        GlobalExceptionHandler.class})
@WebMvcTest(controllers = PartnershipController.class)
class PartnershipControllerValidationStatusTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String VALIDATION_STATUS_URL = "/transactions/" + TRANSACTION_ID
            + "/limited-partnership/partnership/" + SUBMISSION_ID + "/validation-status";

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
    void shouldGetValidationStatusWhenPartnershipIsValid() throws Exception {
        when(partnershipService.validateStatus(transaction)).thenReturn(List.of());

        getRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_valid").value(true))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void shouldGetValidationStatusWithErrorsWhenPartnershipIsNotValid() throws Exception {
        when(partnershipService.validateStatus(transaction)).thenReturn(List.of(
                new ValidationStatusError("Partnership type must not be null", "data.partnershipType", null, null),
                new ValidationStatusError("Email must not be null", "data.email", null, null)));

        getRequest()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_valid").value(false))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[0].error").value("Partnership type must not be null"))
                .andExpect(jsonPath("$.errors[0].location").value("data.partnershipType"))
                .andExpect(jsonPath("$.errors[1].error").value("Email must not be null"))
                .andExpect(jsonPath("$.errors[1].location").value("data.email"));
    }

    @Test
    void shouldReturnNotFoundWhenValidationStatusPartnershipDoesNotExist() throws Exception {
        when(partnershipService.validateStatus(transaction)).thenThrow(new ResourceNotFoundException("error"));

        getRequest()
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInternalServerErrorWhenValidationStatusFails() throws Exception {
        when(partnershipService.validateStatus(transaction)).thenThrow(new ServiceException("TEST"));

        getRequest()
                .andExpect(status().isInternalServerError());
    }

    private ResultActions getRequest() throws Exception {
        return mockMvc.perform(get(VALIDATION_STATUS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .headers(httpHeaders)
                .requestAttr("transaction", transaction));
    }
}

