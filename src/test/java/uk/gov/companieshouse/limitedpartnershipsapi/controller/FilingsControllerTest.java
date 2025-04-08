package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.service.FilingsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FilingsController.class})
@WebMvcTest(controllers = {FilingsController.class})
class FilingsControllerTest {

    private static String URL = "/private/transactions/trn123/incorporation/limited-partnership/sub123/filings";
    private static String INCORPORATION_ID = "sub123";
    private static String REQUEST_ID = "req123";

    private static String JSON_FILING = "";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    FilingsController filingsController;

    @MockitoBean
    FilingsService filingsService;
    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();

        this.mockMvc = MockMvcBuilders.standaloneSetup(filingsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }


    @Test
    void shouldReturn200() throws Exception {
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                )
                .andExpect(status().isOk());
    }

    @Test
    void testWhenFilingIsGenerated() throws ServiceException {
        var filingApi = new FilingApi();
        when(filingsService.generateLimitedPartnerFiling(any())).thenReturn(filingApi);
        var response = filingsController.getFilings(new Transaction(), INCORPORATION_ID, REQUEST_ID, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testWhenError404IsReturned() throws ServiceException {
        when(filingsService.generateLimitedPartnerFiling(any())).thenThrow(ResourceNotFoundException.class);
        var response = filingsController.getFilings(new Transaction(), INCORPORATION_ID, REQUEST_ID, null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testWhenError500IsReturned() throws ServiceException {
        when(filingsService.generateLimitedPartnerFiling(any())).thenThrow(ServiceException.class);
        var response = filingsController.getFilings(new Transaction(), INCORPORATION_ID, REQUEST_ID, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
