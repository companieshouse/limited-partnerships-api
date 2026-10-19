package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import jakarta.validation.Validation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.LimitedPartnershipValidator;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipController;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CompanyService;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.CostsService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        PartnershipController.class,
        GlobalExceptionHandler.class})
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@WebMvcTest(controllers = PartnershipController.class)
class PartnershipControllerUpdateTest {

    private static final String REQUEST_ID = "5346336";
    private static final String USER_ID = "rjg736k791";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;
    private static final String PATCH_URL = "/transactions/" + TRANSACTION_ID + "/limited-partnership/partnership/" + SUBMISSION_ID;

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

    private CompanyService companyService;
    private LimitedPartnershipValidator limitedPartnershipValidator;

    @BeforeEach
    void setUp() {
        companyService = mock(CompanyService.class);
        limitedPartnershipValidator = new LimitedPartnershipValidator(
                Validation.buildDefaultValidatorFactory().getValidator(), companyService);

        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", REQUEST_ID);
        httpHeaders.add("ERIC-Identity", USER_ID);

        transaction = new TransactionBuilder().build();
    }

    @Test
    void shouldUpdatePartnership() throws Exception {
        PartnershipPatchDto patchDto = new PartnershipPatchDto();
        patchDto.setPartnershipName("Test Partnership Updated");

        patchRequest(patchDto)
                .andExpect(status().isOk())
                .andDo(document("partnership-update/success"));

        PartnershipPatchDto sentDto = captureUpdate();
        assertThat(sentDto.getPartnershipName()).isEqualTo("Test Partnership Updated");
    }

    @Test
    void shouldUpdatePartnershipWithDateOfUpdate() throws Exception {
        PartnershipPatchDto patchDto = new PartnershipPatchDto();
        patchDto.setDateOfUpdate(LocalDate.of(2026, 6, 1));

        patchRequest(patchDto)
                .andExpect(status().isOk());

        PartnershipPatchDto sentDto = captureUpdate();
        assertThat(sentDto.getDateOfUpdate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        PartnershipPatchDto patchDto = new PartnershipPatchDto();
        patchDto.setSicCodes(List.of("1234"));

        patchRequest(patchDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.*").value(hasItem("Sic code must be 5 numeric characters")))
                .andDo(document("partnership-update/bad-request-invalid-body"));

        verifyNoInteractions(partnershipService);
    }

    @Test
    void shouldReturnBadRequestWhenDateOfUpdateIsBeforeIncorporation() throws Exception {
        PartnershipPatchDto patchDto = new PartnershipPatchDto();
        patchDto.setDateOfUpdate(LocalDate.of(2022, 6, 1));

        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setDateOfCreation(LocalDate.of(2023, 1, 1));
        when(companyService.getCompanyProfile(transaction.getCompanyNumber())).thenReturn(companyProfileApi);

        doAnswer(invocation -> {
            PartnershipPatchDto sentPatchDto = invocation.getArgument(2);
            PartnershipDto partnershipDto = new PartnershipBuilder()
                    .withDateOfUpdate(sentPatchDto.getDateOfUpdate())
                    .buildDto();
            limitedPartnershipValidator.validateUpdate(partnershipDto, transaction);
            return null;
        }).when(partnershipService)
                .updateLimitedPartnership(eq(transaction), eq(SUBMISSION_ID), any(PartnershipPatchDto.class), eq(REQUEST_ID), eq(USER_ID));

        patchRequest(patchDto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['data.dateOfUpdate']")
                        .value("Limited partnership date of update cannot be before the incorporation date"))
                .andDo(document("partnership-update/bad-request-date-of-update"));

        captureUpdate();
    }

    @Test
    void shouldReturnNotFoundWhenPartnershipDoesNotExist() throws Exception {
        PartnershipPatchDto patchDto = new PartnershipPatchDto();
        patchDto.setPartnershipName("Test Partnership Updated");

        doThrow(new ResourceNotFoundException("Submission " + SUBMISSION_ID + " not found"))
                .when(partnershipService)
                .updateLimitedPartnership(eq(transaction), eq(SUBMISSION_ID), any(PartnershipPatchDto.class), eq(REQUEST_ID), eq(USER_ID));

        patchRequest(patchDto)
                .andExpect(status().isNotFound())
                .andDo(document("partnership-update/not-found"));

        captureUpdate();
    }

    @Test
    void shouldReturnInternalServerErrorWhenUpdatePartnershipFails() throws Exception {
        PartnershipPatchDto patchDto = new PartnershipPatchDto();
        patchDto.setPartnershipName("Test Partnership Updated");

        doThrow(new ServiceException("TEST"))
                .when(partnershipService)
                .updateLimitedPartnership(eq(transaction), eq(SUBMISSION_ID), any(PartnershipPatchDto.class), eq(REQUEST_ID), eq(USER_ID));

        patchRequest(patchDto)
                .andExpect(status().isInternalServerError())
                .andDo(document("partnership-update/internal-server-error"));

        captureUpdate();
    }

    private ResultActions patchRequest(PartnershipPatchDto patchDto) throws Exception {
        return mockMvc.perform(patch(PATCH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .headers(httpHeaders)
                .requestAttr("transaction", transaction)
                .content(jsonMapper.writeValueAsString(patchDto)));
    }

    private PartnershipPatchDto captureUpdate() throws Exception {
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        ArgumentCaptor<PartnershipPatchDto> patchDtoCaptor = ArgumentCaptor.forClass(PartnershipPatchDto.class);

        verify(partnershipService).updateLimitedPartnership(
                transactionCaptor.capture(),
                eq(SUBMISSION_ID),
                patchDtoCaptor.capture(),
                eq(REQUEST_ID),
                eq(USER_ID));

        assertThat(transactionCaptor.getValue()).isSameAs(transaction);
        return patchDtoCaptor.getValue();
    }
}















