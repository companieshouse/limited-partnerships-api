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
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipController;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
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
class PartnershipControllerGetTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String BASE_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/partnership/" + SUBMISSION_ID;

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

    // GET partnership

    @Test
    void shouldGetPartnership() throws Exception {
        PartnershipDto partnershipDto = new PartnershipBuilder().withId(SUBMISSION_ID).buildDto();

        when(partnershipService.getLimitedPartnership(transaction, SUBMISSION_ID)).thenReturn(partnershipDto);

        getRequest(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.partnership_name").value(partnershipDto.getData().getPartnershipName()));
    }

    @Test
    void shouldReturnNotFoundWhenGetPartnershipDoesNotExist() throws Exception {
        when(partnershipService.getLimitedPartnership(transaction, SUBMISSION_ID))
                .thenThrow(new ResourceNotFoundException("Limited Partnership with id " + SUBMISSION_ID + " not found"));

        getRequest(BASE_URL)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInternalServerErrorWhenGetPartnershipFailsUnexpectedly() throws Exception {
        when(partnershipService.getLimitedPartnership(transaction, SUBMISSION_ID))
                .thenThrow(new IllegalStateException("TEST"));

        getRequest(BASE_URL)
                .andExpect(status().isInternalServerError());
    }

    private ResultActions getRequest(String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .headers(httpHeaders)
                .requestAttr("transaction", transaction));
    }
}






