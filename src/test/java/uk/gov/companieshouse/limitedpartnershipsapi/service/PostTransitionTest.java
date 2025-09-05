package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.LimitedPartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class PostTransitionTest {

    @Autowired
    private CostsService costsService;

    @Autowired
    private LimitedPartnershipService limitedPartnershipService;

    @Autowired
    private ValidationStatus validationStatus;

    @MockitoBean
    private LimitedPartnershipRepository limitedPartnershipRepository;

    @MockitoBean
    private TransactionService transactionService;

    private final Transaction transaction = new TransactionBuilder().build();
    private final LimitedPartnershipDao limitedPartnershipDao = new LimitedPartnershipBuilder()
            .withAddresses()
            .withDateOfUpdate(LocalDate.of(2024, 1, 1))
            .buildDao();

    @Test
    void shouldReturn200IfNoErrors() throws Exception {

        mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

        limitedPartnershipDao.getData().setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        var exception = assertThrows(ServiceException.class, () ->
                limitedPartnershipService.validateLimitedPartnership(transaction)
        );

        assertEquals("No strategy found for kind: limited-partnership", exception.getMessage());
    }

    @Nested
    class ValidatePartnershipRegisteredOfficeAddress {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(0, result.size());
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoRegisteredOfficeAddress() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            limitedPartnershipDao.getData().setRegisteredOfficeAddress(null);

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(1, result.size());
            assertThat(result)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.registeredOfficeAddress", "Registered office address is required")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfRegisteredOfficeAddressNotCorrect() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            limitedPartnershipDao.getData().getRegisteredOfficeAddress().setPostalCode(null);

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(1, result.size());
            assertThat(result)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.registeredOfficeAddress.postalCode", "Postcode must not be null")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfRegisteredOfficeAddressPresentButNoDateOfUpdate() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            limitedPartnershipDao.getData().setDateOfUpdate(null);

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(1, result.size());
            assertThat(result)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.dateOfUpdate", "Date of update is required")
                    );

        }
    }

    @Nested
    class ValidatePartnershipName {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(0, result.size());

        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoName() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            limitedPartnershipDao.getData().setPartnershipName(null);
            limitedPartnershipDao.getData().setNameEnding(null);

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(2, result.size());
            assertThat(result)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.partnershipName", "Limited partnership name must not be null"),
                            Map.entry("data.nameEnding", "Name ending is required")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfNameIsTooLong() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            limitedPartnershipDao.getData().setPartnershipName(StringUtils.repeat("A", 161));

            var result = limitedPartnershipService.validateLimitedPartnership(transaction);

            assertEquals(2, result.size());
            assertThat(result)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.partnershipName", "Limited partnership name must be less than 160"),
                            Map.entry("data", "Max length 'partnership name + name ending' is 160 characters")
                    );
        }
    }

    @Nested
    class Costs {
        @Test
        void shouldReturn200AndFeeForKindName() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            var result = costsService.getPostTransitionCost(transaction);

            assertAll("Cost validation",
                    () -> assertEquals("50.00", result.getAmount()),
                    () -> assertEquals("lp-update-partnership-name", result.getProductType()),
                    () -> assertEquals("Update of Limited Partnership name fee", result.getDescription())
            );
        }

        @Test
        void shouldReturn200AndNoFeeForKindROA() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            var result = costsService.getPostTransitionCost(transaction);

            assertNull(result);
        }
    }

    void mocks(PartnershipKind partnershipKind) {
        transaction.setFilingMode("default");

        limitedPartnershipDao.getData().setKind(partnershipKind.getDescription());

        when(limitedPartnershipRepository.findByTransactionId(any())).thenReturn(List.of(limitedPartnershipDao));

        when(transactionService.doesTransactionHaveALimitedPartnership(any(), any())).thenReturn(true);
    }
}
