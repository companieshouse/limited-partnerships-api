package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {LimitedPartnerController.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {LimitedPartnerController.class})
public class LimitedPartnerControllerUpdateTest {
    private static final String LIMITED_PARTNER_ID = "3756304d-fa80-472a-bb6b-8f1f5f04d8eb";
    private static final String LIMITED_PARTNER_URL = "/transactions/863851-951242-143528/limited-partnership/limited-partner/" + LIMITED_PARTNER_ID;

    private static final String JSON_LIMITED_PARTNER_PERSON = """
            {
              "forename": "Joe",
              "surname": "Bloggs",
              "date_of_birth": "2001-01-01",
              "nationality1": "BRITISH",
              "nationality2": null
            }""";

    private static final String JSON_LIMITED_LEGAL_ENTITY = """
            {
                "legal_entity_name": "My Company ltd",
                "legal_form": "Limited Company",
                "governing_law": "Act of law",
                "legal_entity_register_name": "US Register",
                "legal_entity_registration_location": "United States",
                "registered_company_number": "12345678"
            }""";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnerService limitedPartnerService;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");

        transaction = new Transaction();
    }

    @Nested
    class UpdateLimitedPartner {

        @ParameterizedTest
        @ValueSource(strings = {
                JSON_LIMITED_PARTNER_PERSON,
                JSON_LIMITED_LEGAL_ENTITY
        })
        void shouldReturn200(String body) throws Exception {
            mockMvc.perform(patch(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                            .content(body))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class DeleteLimitedPartner {
        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(delete(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Limited partner with id %s not found " + LIMITED_PARTNER_ID))
                    .when(limitedPartnerService)
                    .deleteLimitedPartner(any(), any(), any());

            mockMvc.perform(delete(LIMITED_PARTNER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction))
                    .andExpect(status().isNotFound());
        }
    }
}
