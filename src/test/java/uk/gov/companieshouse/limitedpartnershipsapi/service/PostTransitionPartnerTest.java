package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.GeneralPartnerBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnerKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.generalpartner.dao.GeneralPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.GeneralPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_GENERAL_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_GENERAL_PARTNER;

@SpringBootTest
class PostTransitionPartnerTest {

    @Autowired
    private CostsService costsService;

    @Autowired
    private GeneralPartnerService generalPartnerService;

    @Autowired
    private LimitedPartnerService limitedPartnerService;

    @Autowired
    private ValidationStatus validationStatus;

    @MockitoBean
    private GeneralPartnerRepository generalPartnerRepository;

    @MockitoBean
    private LimitedPartnerRepository limitedPartnerRepository;

    @MockitoBean
    private TransactionService transactionService;

    private final Transaction transactionGeneralPartner = new TransactionBuilder().forPartner(
            FILING_KIND_GENERAL_PARTNER,
            URL_GET_GENERAL_PARTNER,
            GeneralPartnerBuilder.GENERAL_PARTNER_ID
    ).withFilingMode(TransactionService.DEFAULT).build();

    private final GeneralPartnerDao generalPartnerPersonDao = new GeneralPartnerBuilder().personDao();

    @BeforeEach
    void setup() {
        transactionGeneralPartner.setFilingMode(TransactionService.DEFAULT);
    }

    @Test
    void shouldReturn200IfNoKindMatching() {

        mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao);

        generalPartnerPersonDao.getData().setKind(FILING_KIND_GENERAL_PARTNER);

        var exception = assertThrows(ServiceException.class, () ->
                generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId())
        );

        assertEquals(String.format("No strategy found for kind: %s", FILING_KIND_GENERAL_PARTNER), exception.getMessage());
    }

    @Nested
    class RemoveGeneralPartnerPerson {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {

            generalPartnerPersonDao.getData().setCeaseDate(LocalDate.of(2025, 1, 1));
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(true);

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoCeaseDate() throws Exception {

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao);

            generalPartnerPersonDao.getData().setCeaseDate(null);
            generalPartnerPersonDao.getData().setRemoveConfirmationChecked(false);

            var result = generalPartnerService.validateGeneralPartner(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.ceaseDate", "Cease date is required"),
                            Map.entry("data.removeConfirmationChecked", "Remove confirmation checked is required")
                    );
        }

        @Test
        void shouldReturn200AndNoFeeForKindROA() throws Exception {

            mocks(PartnerKind.REMOVE_GENERAL_PARTNER_PERSON, generalPartnerPersonDao);

            var result = costsService.getPostTransitionGeneralPartnerCost(transactionGeneralPartner, generalPartnerPersonDao.getId());

            assertNull(result);
        }
    }

    void mocks(PartnerKind partnerKind, GeneralPartnerDao generalPartnerDao) {
        generalPartnerPersonDao.getData().setKind(partnerKind.getDescription());

        when(generalPartnerRepository.findById(any())).thenReturn(Optional.of(generalPartnerDao));

        when(transactionService.isTransactionLinkedToPartner(any(), any(), any())).thenReturn(true);
    }
}
