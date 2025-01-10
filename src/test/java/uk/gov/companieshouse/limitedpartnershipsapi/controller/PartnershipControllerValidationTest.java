package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PartnershipController.class})
@WebMvcTest(controllers = {PartnershipController.class})
class PartnershipControllerValidationTest {

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionInterceptor transactionInterceptor;

    @Autowired
    private PartnershipController partnershipController;

    @MockBean
    private LimitedPartnershipService service;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();

        this.mockMvc = MockMvcBuilders.standaloneSetup(partnershipController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

    }

    @Test
    void testCreatePartnershipShouldReturn201() throws Exception {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();
        DataDto dto = new DataDto();

        dto.setPartnershipName("test name");
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        limitedPartnershipSubmissionDto.setData(dto);

        String body = objectMapper.writeValueAsString(limitedPartnershipSubmissionDto);

        mockMvc.perform(post("/transactions/863851-951242-143528/limited-partnership/partnership")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreatePartnershipShouldReturnBadRequestErrorIfPartnershipNameIsLessThan1Character() throws Exception {

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = new LimitedPartnershipSubmissionDto();

        DataDto dto = new DataDto();
        dto.setPartnershipName("");
        dto.setNameEnding(PartnershipNameEnding.LIMITED_PARTNERSHIP);
        dto.setPartnershipType(PartnershipType.LP);
        limitedPartnershipSubmissionDto.setData(dto);

        String body = objectMapper.writeValueAsString(limitedPartnershipSubmissionDto);

        mockMvc.perform(post("/transactions/863851-951242-143528/limited-partnership/partnership")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePartnershipShouldReturn200() throws Exception {
        String body = "{\"email\":\"test@email.com\"}";

        mockMvc.perform(patch("/transactions/863851-951242-143528/limited-partnership/partnership/93702824-9062-4c63-a694-716acffccdd5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePartnershipShouldReturnBadRequestErrorIfEmailBadlyFormated() throws Exception {
        String body = "{\"email\":\"test@email.\"}";

        mockMvc.perform(patch("/transactions/863851-951242-143528/limited-partnership/partnership/93702824-9062-4c63-a694-716acffccdd5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePartnershipShouldReturn200IfNameSizeIsCorrect() throws Exception {
        String body = "{\"partnership_name\":\"Correct name size\",\"name_ending\":\"Limited Partnership\"}";

        mockMvc.perform(patch("/transactions/863851-951242-143528/limited-partnership/partnership/93702824-9062-4c63-a694-716acffccdd5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePartnershipShouldReturnBadRequestErrorIfNameSizeIsTooLong() throws Exception {
        String body = "{\"partnership_name\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"name_ending\":\"Limited Partnership\"}";

        mockMvc.perform(patch("/transactions/863851-951242-143528/limited-partnership/partnership/93702824-9062-4c63-a694-716acffccdd5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
