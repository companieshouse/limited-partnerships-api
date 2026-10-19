package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.LimitedPartnershipValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipController;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CostsService;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ContextConfiguration(classes = {
        PartnershipController.class,
        GlobalExceptionHandler.class})
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@WebMvcTest(controllers = PartnershipController.class)
class PartnershipControllerCreateTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String POST_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/partnership";

    private HttpHeaders httpHeaders;
    private Transaction transaction;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private PartnershipService partnershipService;

    @MockitoBean
    private CostsService costsService;

    private LimitedPartnershipValidator limitedPartnershipValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        limitedPartnershipValidator = new LimitedPartnershipValidator(validator, mock(CompanyService.class));

        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", REQUEST_ID);
        httpHeaders.add("ERIC-Identity", USER_ID);

        transaction = new TransactionBuilder().build();
    }

    @Test
    void shouldCreatePartnership() throws Exception {
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
        String body = jsonMapper.writeValueAsString(partnershipDto);

        when(partnershipService.createLimitedPartnership(eq(transaction), any(PartnershipDto.class), eq(REQUEST_ID), eq(USER_ID)))
                .thenReturn(SUBMISSION_ID);

        mockMvc.perform(post(POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, String.format(URL_GET_PARTNERSHIP, TRANSACTION_ID, SUBMISSION_ID)))
                .andExpect(jsonPath("$.id").value(SUBMISSION_ID))
                .andDo(document("partnership-create/success"));

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        ArgumentCaptor<PartnershipDto> partnershipDtoCaptor = ArgumentCaptor.forClass(PartnershipDto.class);
        verify(partnershipService).createLimitedPartnership(
                transactionCaptor.capture(),
                partnershipDtoCaptor.capture(),
                eq(REQUEST_ID),
                eq(USER_ID));

        assertThat(transactionCaptor.getValue()).isSameAs(transaction);
        assertThat(partnershipDtoCaptor.getValue().getData())
                .usingRecursiveComparison()
                .isEqualTo(partnershipDto.getData());
    }

    @Test
    void shouldReturnInternalServerErrorWhenCreatePartnershipFails() throws Exception {
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
        String body = jsonMapper.writeValueAsString(partnershipDto);

        when(partnershipService.createLimitedPartnership(eq(transaction), any(PartnershipDto.class), eq(REQUEST_ID), eq(USER_ID)))
                .thenThrow(new ServiceException("TEST"));

        mockMvc.perform(post(POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andDo(document("partnership-create/internal-server-error"));

        verify(partnershipService).createLimitedPartnership(eq(transaction), any(PartnershipDto.class), eq(REQUEST_ID), eq(USER_ID));
    }

    @Test
    void shouldReturnBadRequestWithErrorsWhenPartnershipIsInvalid() throws Exception {
        PartnershipDto partnershipDto = new PartnershipBuilder()
                .withNameEnding(null)
                .withPartnershipType(null)
                .buildDto();
        partnershipDto.getData().setPartnershipName(null);
        String body = jsonMapper.writeValueAsString(partnershipDto);

        when(partnershipService.createLimitedPartnership(eq(transaction), any(PartnershipDto.class), eq(REQUEST_ID), eq(USER_ID)))
                .thenAnswer(invocation -> {
                    PartnershipDto dto = invocation.getArgument(1);
                    limitedPartnershipValidator.validatePartial(dto, FilingMode.fromDescription(transaction.getFilingMode()));
                    return SUBMISSION_ID;
                });

        mockMvc.perform(post(POST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .headers(httpHeaders)
                        .requestAttr("transaction", transaction)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors['data.partnershipName']").value("Limited partnership name must not be null"))
                .andExpect(jsonPath("$.errors['data.nameEnding']").value("Name ending is required"))
                .andExpect(jsonPath("$.errors['data.partnershipType']").value("Partnership type must not be null"))
                .andDo(document("partnership-create/bad-request"));

        verify(partnershipService).createLimitedPartnership(eq(transaction), any(PartnershipDto.class), eq(REQUEST_ID), eq(USER_ID));
    }
}




















