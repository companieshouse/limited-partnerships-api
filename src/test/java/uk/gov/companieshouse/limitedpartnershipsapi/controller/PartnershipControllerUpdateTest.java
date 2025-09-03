package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.LimitedPartnershipValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {PartnershipController.class, LimitedPartnershipService.class, LimitedPartnershipValidator.class, ValidationStatus.class, LimitedPartnershipMapperImpl.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {PartnershipController.class})
class PartnershipControllerUpdateTest {

    private static final String PARTNERSHIP_PATCH_URL = "/transactions/" + TransactionBuilder.TRANSACTION_ID + "/limited-partnership/partnership/" + LimitedPartnershipBuilder.SUBMISSION_ID;

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LimitedPartnershipRepository limitedPartnershipRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }

    @Test
    void UpdateAddressShouldReturn200() throws Exception {
        mocks();

        LimitedPartnershipDto limitedPartnership = new LimitedPartnershipBuilder().withAddresses().buildDto();

        ObjectWriter jsonWrapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = jsonWrapper.writeValueAsString(limitedPartnership.getData().getRegisteredOfficeAddress());

        String body = "{ \"registered_office_address\": " + json + " }";

        mockMvc.perform(patch(PARTNERSHIP_PATCH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void UpdateDateShouldReturn200() throws Exception {
        mocks();

        String body = "{ \"date_of_update\" : \"2024-01-01\" }";

        mockMvc.perform(patch(PARTNERSHIP_PATCH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void UpdateDateShouldReturn400IfDateInFuture() throws Exception {
        mocks();

        String body = "{ \"date_of_update\" : \"2030-01-01\" }";

        mockMvc.perform(patch(PARTNERSHIP_PATCH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UpdateNameShouldReturn200() throws Exception {
        mocks();

        String body = "{ \"partnership_name\" : \"Test name\", \"name_ending\" : \"LP\" }";

        mockMvc.perform(patch(PARTNERSHIP_PATCH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void UpdateNameShouldReturn400IfNameEndingIsNotCorrect() throws Exception {
        mocks();

        String body = "{ \"partnership_name\" : \"Test name\", \"name_ending\" : \"PP\" }";

        mockMvc.perform(patch(PARTNERSHIP_PATCH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private void mocks(LimitedPartnershipDao limitedPartnershipDao) throws ServiceException {
        when(limitedPartnershipRepository.insert((LimitedPartnershipDao) any())).thenReturn(limitedPartnershipDao);
        when(limitedPartnershipRepository.save(any())).thenReturn(limitedPartnershipDao);
        when(limitedPartnershipRepository.findById(LimitedPartnershipBuilder.SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipDao));

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);

        LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();

        when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(limitedPartnershipDto);
    }

    private void mocks() throws ServiceException {
        LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder().buildDao();

        mocks(limitedPartnershipDao);
    }

}
