package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode;
import uk.gov.companieshouse.limitedpartnershipsapi.model.common.PartnershipKind;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.service.validator.ValidationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@SpringBootTest
class PostTransitionPartnershipTest {

    @Autowired
    private CostsService costsService;

    @Autowired
    private PartnershipService partnershipService;

    @Autowired
    private ValidationStatus validationStatus;

    @MockitoBean
    private PartnershipRepository partnershipRepository;

    @MockitoBean
    private TransactionService transactionService;

    private final Transaction transaction = new TransactionBuilder().build();
    private final PartnershipDao partnershipDao = new PartnershipBuilder()
            .withAddresses()
            .withDateOfUpdate(LocalDate.of(2024, 1, 1))
            .buildDao();

    @Test
    void shouldReturn200IfNoKindMatching() {

        mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

        partnershipDao.getData().setKind(FILING_KIND_LIMITED_PARTNERSHIP);

        var exception = assertThrows(ServiceException.class, () ->
            partnershipService.validateLimitedPartnership(transaction)
        );

        assertEquals("No strategy found for kind: limited-partnership", exception.getMessage());
    }

    @Nested
    class ValidatePartnershipRegisteredOfficeAddress {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoRegisteredOfficeAddress() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            partnershipDao.getData().setRegisteredOfficeAddress(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.registeredOfficeAddress", "Registered office address is required")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfRegisteredOfficeAddressNotCorrect() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            partnershipDao.getData().getRegisteredOfficeAddress().setPostalCode(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.registeredOfficeAddress.postalCode", "Postcode must not be null")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfRegisteredOfficeAddressPresentButNoDateOfUpdate() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            partnershipDao.getData().setDateOfUpdate(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
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

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoName() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            partnershipDao.getData().setPartnershipName(null);
            partnershipDao.getData().setNameEnding(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.partnershipName", "Limited partnership name must not be null"),
                            Map.entry("data.nameEnding", "Name ending is required")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfNameIsTooLong() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            partnershipDao.getData().setPartnershipName(StringUtils.repeat("A", 161));

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(2)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.partnershipName", "Limited partnership name must be less than 160"),
                            Map.entry("data", "Max length 'partnership name + name ending' is 160 characters")
                    );
        }
    }

    @Nested
    class ValidateTerm {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_TERM);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoTerm() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_TERM);

            partnershipDao.getData().setTerm(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.term", "Term is required")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfTermIsIncorrect() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_TERM);

            partnershipDao.getData().setTerm(Term.UNKNOWN);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.term", "Term must be valid")
                    );
        }
    }

    @Nested
    class ValidatePartnershipPrincipalPlaceOfBusinessAddress {
        @Test
        void shouldReturn200IfNoErrors() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_PRINCIPAL_PLACE_OF_BUSINESS_ADDRESS);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturn200AndErrorDetailsIfNoRegisteredOfficeAddress() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_PRINCIPAL_PLACE_OF_BUSINESS_ADDRESS);

            partnershipDao.getData().setPrincipalPlaceOfBusinessAddress(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.principalPlaceOfBusinessAddress", "Principal place of business address is required")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfRegisteredOfficeAddressNotCorrect() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_PRINCIPAL_PLACE_OF_BUSINESS_ADDRESS);

            partnershipDao.getData().getPrincipalPlaceOfBusinessAddress().setPostalCode(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.principalPlaceOfBusinessAddress.postalCode", "Postcode must not be null")
                    );

        }

        @Test
        void shouldReturn200AndErrorDetailsIfRegisteredOfficeAddressPresentButNoDateOfUpdate() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_PRINCIPAL_PLACE_OF_BUSINESS_ADDRESS);

            partnershipDao.getData().setDateOfUpdate(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            assertThat(result).hasSize(1)
                    .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                    .containsExactlyInAnyOrder(
                            Map.entry("data.dateOfUpdate", "Date of update is required")
                    );
        }
    }

    @Nested
    class ValidateRedesignateToPFLP {

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200IfNoErrorsResignateToPFLPFieldsAreBothTrueUnlessPrivate(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(Boolean.TRUE);
            partnershipDao.getData().setRedesignateToPFLPConfirm(Boolean.TRUE);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).isEmpty();
            } else {
                assertThat(result).hasSize(1)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied")
                        );
            }
        }

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200IfNoErrorsResignateToConfirmPFLPIsFalse(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(Boolean.TRUE);
            partnershipDao.getData().setRedesignateToPFLPConfirm(Boolean.FALSE);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).hasSize(1)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            } else {
                assertThat(result).hasSize(2)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied"),
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")

                        );
            }
        }

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200IfNoErrorsResignateToApplyPFLPIsFalse(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(Boolean.FALSE);
            partnershipDao.getData().setRedesignateToPFLPConfirm(Boolean.TRUE);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).hasSize(1)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required")
                        );
            } else {
                assertThat(result).hasSize(2)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied"),
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required")

                        );
            }
        }

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200AndNoErrorDetailsIfRedisgnateFlagsAreBothFalse(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(Boolean.FALSE);
            partnershipDao.getData().setRedesignateToPFLPConfirm(Boolean.FALSE);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).hasSize(2)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required"),
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            } else {
                assertThat(result).hasSize(3)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied"),
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required"),
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            }
        }

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200AndNoErrorDetailsIfResignateToConfirmPFLPIsNull(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(Boolean.TRUE);
            partnershipDao.getData().setRedesignateToPFLPConfirm(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).hasSize(1)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            } else {
                assertThat(result).hasSize(2)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied"),
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            }
        }

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200AndNoErrorDetailsIfResignateToApplyPFLPIsNull(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(null);
            partnershipDao.getData().setRedesignateToPFLPConfirm(Boolean.TRUE);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).hasSize(1)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required")
                        );
            } else {
                assertThat(result).hasSize(2)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied"),
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required")
                        );
            }
        }

        @ParameterizedTest
        @EnumSource(value = PartnershipType.class, names = {"LP", "PFLP", "SLP", "SPFLP"})
        void shouldReturn200AndNoErrorDetailsIfRedisgnateFlagsAreBothNull(PartnershipType partnershipType) throws Exception {
            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            partnershipDao.getData().setPartnershipType(partnershipType);
            partnershipDao.getData().setRedesignateToPFLPApply(null);
            partnershipDao.getData().setRedesignateToPFLPConfirm(null);

            var result = partnershipService.validateLimitedPartnership(transaction);

            if (partnershipType == PartnershipType.LP || partnershipType == PartnershipType.SLP) {
                assertThat(result).hasSize(2)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required"),
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            } else {
                assertThat(result).hasSize(3)
                        .extracting(e -> Map.entry(e.getLocation(), e.getError()))
                        .containsExactlyInAnyOrder(
                                Map.entry("data.partnershipType", "Incorrect partnership type supplied"),
                                Map.entry("data.redesignateToPFLPApply", "Apply to redesignate to pflp check is required"),
                                Map.entry("data.redesignateToPFLPConfirm", "Confirm redesignate to pflp check is required")
                        );
            }
        }
    }

    @Nested
    class Costs {
        @Test
        void shouldReturn200AndFeeForKindName() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_NAME);

            var result = costsService.getPostTransitionLimitedPartnershipCost(transaction);

            assertAll("Cost validation",
                    () -> assertEquals("50.00", result.getAmount()),
                    () -> assertEquals("lp-update-partnership-name", result.getProductType()),
                    () -> assertEquals("Update of Limited Partnership name fee", result.getDescription())
            );
        }

        @Test
        void shouldReturn200AndNoFeeForKindROA() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REGISTERED_OFFICE_ADDRESS);

            var result = costsService.getPostTransitionLimitedPartnershipCost(transaction);

            assertNull(result);
        }

        @Test
        void shouldReturn200AndNoFeeForKindPPOBA() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_PRINCIPAL_PLACE_OF_BUSINESS_ADDRESS);

            var result = costsService.getPostTransitionLimitedPartnershipCost(transaction);

            assertNull(result);
        }

        @Test
        void shouldReturn200AndNoFeeForKindRedesignateToPFLP() throws Exception {

            mocks(PartnershipKind.UPDATE_PARTNERSHIP_REDESIGNATE_TO_PFLP);

            var result = costsService.getPostTransitionLimitedPartnershipCost(transaction);

            assertAll("Cost validation",
                    () -> assertEquals("1.00", result.getAmount()),
                    () -> assertEquals("lp-update-partnership-redesignate-to-pflp", result.getProductType()),
                    () -> assertEquals("Redesignate to pflp fee", result.getDescription())
            );
        }
    }

    void mocks(PartnershipKind partnershipKind) {
        transaction.setFilingMode(FilingMode.DEFAULT.getDescription());

        partnershipDao.getData().setKind(partnershipKind.getDescription());

        when(partnershipRepository.findByTransactionId(any())).thenReturn(List.of(partnershipDao));

        when(transactionService.doesTransactionHaveALimitedPartnership(any(), any())).thenReturn(true);
    }
}
