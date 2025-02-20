package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GeneralPartnerController.class})
@WebMvcTest(controllers = {GeneralPartnerController.class})
class GeneralPartnerControllerValidationTest {

    static String postUrl = "/transactions/863851-951242-143528/limited-partnership/general-partner";

    private static final String JSON_WITH_BELOW_MIN_FORENAME = "{\n" +
            "    \"data\": {\n" +
            "      \"forename\": \"\",\n" +
            "      \"former_names\": \"\",\n" +
            "      \"surname\": \"Bloggs\",\n" +
            "      \"date_of_birth\": \"2001-01-01\",\n" +
            "      \"nationality1\": \"British\",\n" +
            "      \"nationality2\": \"\",\n" +
            "      \"kind\": \"kind\",\n" +
            "      \"etag\": \"tag\",\n" +
            "      \"general_partner_type\": \"Person\"\n" +
            "    }\n" +
            "}";
    private static final String JSON_WITH_ABOVE_MAX_FORENAME = "{\n" +
            "    \"data\": {\n" +
            "      \"forename\": \"The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog\"  ,\n" +
            "      \"former_names\": \"\",\n" +
            "      \"surname\": \"Bloggs\",\n" +
            "      \"date_of_birth\": \"2001-01-01\",\n" +
            "      \"nationality1\": \"British\",\n" +
            "      \"nationality2\": \"\",\n" +
            "      \"kind\": \"kind\",\n" +
            "      \"etag\": \"tag\",\n" +
            "      \"general_partner_type\": \"Person\"\n" +
            "    }\n" +
            "}";
    private static final String JSON_WITH_BELOW_MIN_SURNAME = "{\n" +
            "    \"data\": {\n" +
            "      \"forename\": \"Joe\",\n" +
            "      \"former_names\": \"\",\n" +
            "      \"surname\": \"\",\n" +
            "      \"date_of_birth\": \"2001-01-01\",\n" +
            "      \"nationality1\": \"British\",\n" +
            "      \"nationality2\": \"\",\n" +
            "      \"kind\": \"kind\",\n" +
            "      \"etag\": \"tag\",\n" +
            "      \"general_partner_type\": \"Person\"\n" +
            "    }\n" +
            "}";
    private static final String JSON_WITH_ABOVE_MAX_SURNAME = "{\n" +
            "    \"data\": {\n" +
            "      \"forename\": \"Joe\",\n" +
            "      \"former_names\": \"\",\n" +
            "      \"surname\":\"The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog\",\n" +
            "      \"date_of_birth\": \"2001-01-01\",\n" +
            "      \"nationality1\": \"British\",\n" +
            "      \"nationality2\": \"\",\n" +
            "      \"kind\": \"kind\",\n" +
            "      \"etag\": \"tag\",\n" +
            "      \"general_partner_type\": \"Person\"\n" +
            "    }\n" +
            "}";
    private static final String JSON_WITH_ABOVE_MAX_FORMERNAME = "{\n" +
            "    \"data\": {\n" +
            "      \"forename\": \"Joe\",\n" +
            "      \"former_names\": \"The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog " +
            "The quick brown fox jumps over the lazy dog\",\n" +
            "      \"surname\": \"Bloggs\",\n" +
            "      \"date_of_birth\": \"2001-01-01\",\n" +
            "      \"nationality1\": \"British\",\n" +
            "      \"nationality2\": \"\",\n" +
            "      \"kind\": \"kind\",\n" +
            "      \"etag\": \"tag\",\n" +
            "      \"general_partner_type\": \"Person\"\n" +
            "    }\n" +
            "}";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    GeneralPartnerController generalPartnerController;

    @MockBean
    GeneralPartnerService generalPartnerService;

    @MockBean
    private TransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();

        this.mockMvc = MockMvcBuilders.standaloneSetup(generalPartnerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn201()  throws Exception {
        GeneralPartnerDto dto = createDto();
        dto.getData().setSurname("Bloggs");
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(GeneralPartnerControllerValidationTest.postUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            JSON_WITH_BELOW_MIN_FORENAME,
            JSON_WITH_ABOVE_MAX_FORENAME,
            JSON_WITH_BELOW_MIN_SURNAME,
            JSON_WITH_ABOVE_MAX_SURNAME,
            JSON_WITH_ABOVE_MAX_FORMERNAME
    })
    void shouldReturn400(String body)  throws Exception {
        mockMvc.perform(post(GeneralPartnerControllerValidationTest.postUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private static String createLongString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 161; i++) {
            sb.append("a");
        }
        return sb.toString();
    }
    private GeneralPartnerDto createDto() {
        GeneralPartnerDto dto = new GeneralPartnerDto();
        GeneralPartnerDataDto dataDto = new GeneralPartnerDataDto();
        dataDto.setPartnerType(LimitedPartnerType.LEGAL_ENTITY);
        dto.setData(dataDto);
        return dto;
    }
}
