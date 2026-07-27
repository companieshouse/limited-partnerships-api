package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.CompanyBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.incorporation.IncorporationRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PartnershipMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PartnershipPatchMapperImpl;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.CostsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.LimitedPartnershipValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.posttransition.PostTransitionStrategyHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        PartnershipController.class,
    PartnershipService.class,
        LimitedPartnershipValidator.class,
        ValidationStatus.class,
    PartnershipMapperImpl.class,
    PartnershipPatchMapperImpl.class,
        CostsService.class,
        PostTransitionStrategyHandler.class,
        GlobalExceptionHandler.class
})
@WebMvcTest(controllers = {PartnershipController.class})
class PartnershipControllerUpdateTest {

    private static final String PARTNERSHIP_PATCH_URL = "/transactions/" + TransactionBuilder.TRANSACTION_ID + "/limited-partnership/partnership/" + PartnershipBuilder.SUBMISSION_ID;

    private HttpHeaders httpHeaders;
    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PartnershipRepository partnershipRepository;

    @MockitoBean
    private IncorporationRepository incorporationRepository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CostsService costsService;

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

        PartnershipDto limitedPartnership = new PartnershipBuilder().withAddresses().buildDto();

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
    void UpdateDateShouldReturn200IfItIsToday() throws Exception {
        mocks();

        LocalDate today = LocalDate.now();
        String body = "{ \"date_of_update\" : \"" + today.toString() + "\" }";

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['dateOfUpdate']").value("Date of update must not be in the future"));
    }

    @Test
    void UpdateDateShouldReturn400IfDateIsBeforeCompanyDateOfCreation() throws Exception {
        mocks();

        String body = "{ \"date_of_update\" : \"2020-01-01\" }";

        mockMvc.perform(patch(PARTNERSHIP_PATCH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['errors'].['data.dateOfUpdate']").value("Limited partnership date of update cannot be before the incorporation date"));
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

    private void mocks(PartnershipDao partnershipDao) throws ServiceException {
        when(partnershipRepository.insert((PartnershipDao) any())).thenReturn(partnershipDao);
        when(partnershipRepository.save(any())).thenReturn(partnershipDao);
        when(partnershipRepository.findById(PartnershipBuilder.SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        CompanyProfileApi companyProfile = new CompanyBuilder().build();
        when(companyService.getCompanyProfile(any())).thenReturn(companyProfile);
    }

    private void mocks() throws ServiceException {
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

        mocks(partnershipDao);
    }
}
