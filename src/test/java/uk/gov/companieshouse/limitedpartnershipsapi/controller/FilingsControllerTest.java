package uk.gov.companieshouse.limitedpartnershipsapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dto.GeneralPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.incorporation.IncorporationKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.limitedpartner.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.service.FilingsService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.GeneralPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnerService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.LimitedPartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.FilingKind;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ContextConfiguration(classes = {FilingsController.class, FilingsService.class, FilingKind.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = {FilingsController.class})
class FilingsControllerTest {

    private HttpHeaders httpHeaders;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilingsController filingsController;

    @MockitoBean
    private LimitedPartnershipService limitedPartnershipService;

    @MockitoBean
    private GeneralPartnerService generalPartnerService;

    @MockitoBean
    private LimitedPartnerService limitedPartnerService;

    @MockitoBean
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", "passthrough");
        httpHeaders.add("X-Request-Id", "123");
        httpHeaders.add("ERIC-Identity", "123");
    }

    LimitedPartnershipDto limitedPartnershipDto = new LimitedPartnershipBuilder().buildDto();
    GeneralPartnerDto generalPartner = new GeneralPartnerBuilder().personDto();
    LimitedPartnerDto limitedPartner = new LimitedPartnerBuilder().personDto();

    @Nested
    class IncorporationFilling {
        private static final String URL = "/private/transactions/" + TransactionBuilder.TRANSACTION_ID + "/incorporation/limited-partnership/" + LimitedPartnershipBuilder.SUBMISSION_ID + "/filings";
        private final Transaction transaction = new TransactionBuilder().build();

        @Test
        void shouldReturn200() throws Exception {
            mock(transaction);

            mockMvc.perform(get(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("[0].data.limited_partnership.partnership_name").value(limitedPartnershipDto.getData().getPartnershipName()))
                    .andExpect(jsonPath("[0].data.limited_partnership.name_ending").value(limitedPartnershipDto.getData().getNameEnding()));
        }

        @Test
        void shouldReturn404() throws Exception {
            mock(transaction);

            when(limitedPartnershipService.getLimitedPartnership(transaction)).thenThrow(ResourceNotFoundException.class);

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
            mock(transaction);

            when(limitedPartnershipService.getLimitedPartnership(transaction)).thenThrow(ServiceException.class);

            mockMvc.perform(get(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                    )
                    .andExpect(status().isInternalServerError());
        }

        private void mock(Transaction transaction) throws ServiceException {

            when(transactionService.isTransactionLinkedToLimitedPartnershipIncorporation(eq(transaction), any(String.class))).thenReturn(true);
            when(limitedPartnershipService.getLimitedPartnership(transaction)).thenReturn(limitedPartnershipDto);
            when(generalPartnerService.getGeneralPartnerDataList(transaction)).thenReturn(new ArrayList<>());
            when(limitedPartnerService.getLimitedPartnerDataList(transaction)).thenReturn(new ArrayList<>());
        }
    }

    @Nested
    class GeneralPartnerFilling {
        private static final String URL = "/private/transactions/" + TransactionBuilder.TRANSACTION_ID + "/limited-partnership/general-partner/" + GeneralPartnerBuilder.GENERAL_PARTNER_ID + "/filings";
        private final Transaction transaction = new TransactionBuilder().forPartner(
                PartnerKind.ADD_GENERAL_PARTNER_PERSON.getDescription(),
                URL_GET_GENERAL_PARTNER,
                GeneralPartnerBuilder.GENERAL_PARTNER_ID
        ).build();

        @Test
        void shouldReturn200() throws Exception {

            FilingKind filingKind = new FilingKind();
            String subKind = filingKind.getSubKind(PartnerKind.ADD_GENERAL_PARTNER_PERSON.getDescription());

            mockPartner(transaction, PartnerKind.ADD_GENERAL_PARTNER_PERSON.getDescription());

            mockMvc.perform(get(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("[0].data.general_partners[0].forename").value(generalPartner.getData().getForename()))
                    .andExpect(jsonPath("[0].data.general_partners[0].surname").value(generalPartner.getData().getSurname()))
                    .andExpect(jsonPath("[0].data.general_partners[0].kind").value(PartnerKind.ADD_GENERAL_PARTNER_PERSON.getDescription()))
                    .andExpect(jsonPath("[0].kind").value(IncorporationKind.POST_TRANSITION.getDescription() + "#" + subKind));
        }

        @Test
        void shouldReturn404() throws Exception {
            mockPartner(transaction, PartnerKind.ADD_GENERAL_PARTNER_PERSON.getDescription());

            when(generalPartnerService.getGeneralPartner(transaction, GeneralPartnerBuilder.GENERAL_PARTNER_ID)).thenThrow(ResourceNotFoundException.class);

            mockMvc.perform(get(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                    )
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class LimitedPartnerFilling {
        private static final String URL = "/private/transactions/" + TransactionBuilder.TRANSACTION_ID + "/limited-partnership/limited-partner/" + LimitedPartnerBuilder.LIMITED_PARTNER_ID + "/filings";
        private final Transaction transaction = new TransactionBuilder().forPartner(
                PartnerKind.ADD_LIMITED_PARTNER_PERSON.getDescription(),
                URL_GET_LIMITED_PARTNER,
                LimitedPartnerBuilder.LIMITED_PARTNER_ID
        ).build();

        @Test
        void shouldReturn200() throws Exception {

            FilingKind filingKind = new FilingKind();
            String subKind = filingKind.getSubKind(PartnerKind.ADD_LIMITED_PARTNER_PERSON.getDescription());

            mockPartner(transaction, PartnerKind.ADD_LIMITED_PARTNER_PERSON.getDescription());

            mockMvc.perform(get(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("[0].data.limited_partners[0].forename").value(limitedPartner.getData().getForename()))
                    .andExpect(jsonPath("[0].data.limited_partners[0].surname").value(limitedPartner.getData().getSurname()))
                    .andExpect(jsonPath("[0].data.limited_partners[0].kind").value(PartnerKind.ADD_LIMITED_PARTNER_PERSON.getDescription()))
                    .andExpect(jsonPath("[0].kind").value(IncorporationKind.POST_TRANSITION.getDescription() + "#" + subKind));
        }

        @Test
        void shouldReturn404() throws Exception {
            mockPartner(transaction, PartnerKind.ADD_LIMITED_PARTNER_PERSON.getDescription());

            when(limitedPartnerService.getLimitedPartner(transaction, LimitedPartnerBuilder.LIMITED_PARTNER_ID)).thenThrow(ResourceNotFoundException.class);

            mockMvc.perform(get(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .headers(httpHeaders)
                            .requestAttr("transaction", transaction)
                    )
                    .andExpect(status().isNotFound());
        }

    }

    private void mockPartner(Transaction transaction, String kind) throws ResourceNotFoundException {

        generalPartner.getData().setKind(PartnerKind.ADD_GENERAL_PARTNER_PERSON.getDescription());
        limitedPartner.getData().setKind(PartnerKind.ADD_LIMITED_PARTNER_PERSON.getDescription());

        when(transactionService.isTransactionLinkedToPartner(eq(transaction), any(String.class), eq(kind))).thenReturn(true);
        when(generalPartnerService.getGeneralPartner(transaction, generalPartner.getId())).thenReturn(generalPartner);
        when(limitedPartnerService.getLimitedPartner(transaction, limitedPartner.getId())).thenReturn(limitedPartner);
    }
}
