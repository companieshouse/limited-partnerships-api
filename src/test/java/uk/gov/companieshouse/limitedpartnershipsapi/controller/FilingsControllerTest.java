package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.FilingsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LIMITED_PARTNERSHIP_FIELD;

@ContextConfiguration(classes = {FilingsController.class})
@WebMvcTest(controllers = {FilingsController.class})
class FilingsControllerTest {

    private static String URL = "/private/transactions/trn123/incorporation/limited-partnership/sub123/filings";
    private static String KIND = "limited_partnerships";

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
    }

    @Test
    void shouldReturn200() throws Exception {
        when(filingsService.generateLimitedPartnerFiling(transaction)).thenReturn(buildFilingApi());
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].data.limited_partnership.partnership_name").value("Test Partnership"))
                .andExpect(jsonPath("[0].data.limited_partnership.name_ending").value(PartnershipNameEnding.LP.getDescription()));
    }

    @Test
    void shouldReturn404() throws Exception {
        when(filingsService.generateLimitedPartnerFiling(any())).thenThrow(ResourceNotFoundException.class);
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500() throws Exception {
        when(filingsService.generateLimitedPartnerFiling(any())).thenThrow(ServiceException.class);
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                )
                .andExpect(status().isInternalServerError());
    }

    private FilingApi buildFilingApi() {
        FilingApi filingApi = new FilingApi();
        filingApi.setKind(KIND);
        Map<String, Object> data = new HashMap<>();
        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipDto();
        DataDto dataDto = new DataDto();
        dataDto.setPartnershipName("Test Partnership");
        dataDto.setNameEnding(PartnershipNameEnding.LP);
        limitedPartnershipDto.setData(dataDto);
        data.put(LIMITED_PARTNERSHIP_FIELD, limitedPartnershipDto.getData());
        filingApi.setData(data);
        return filingApi;
    }
}
