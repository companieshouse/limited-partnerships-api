package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipType;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.Term;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceValidateStatusTest extends AbstractPartnershipServiceTestSupport {

    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private PartnershipService partnershipService;

    @MockitoBean
    private PartnershipRepository partnershipRepository;

    @Test
    void shouldReturnNoErrorsWhenRegistrationPartnershipIsValid() throws ServiceException {
        PartnershipDao partnershipDao = validRegistrationDao();

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(partnershipDao));

        List<ValidationStatusError> errors = partnershipService.validateStatus(transaction);

        verify(partnershipRepository).findByTransactionId(transaction.getId());
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenRegistrationPartnershipIsMissingRequiredFields() throws ServiceException {
        PartnershipDao partnershipDao = validRegistrationDao();
        partnershipDao.getData().setNameEnding(null);
        partnershipDao.getData().setEmail(null);
        partnershipDao.getData().setJurisdiction(null);
        partnershipDao.getData().setRegisteredOfficeAddress(null);
        partnershipDao.getData().setPrincipalPlaceOfBusinessAddress(null);
        partnershipDao.getData().setLawfulPurposeStatementChecked(null);

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(partnershipDao));

        List<ValidationStatusError> errors = partnershipService.validateStatus(transaction);

        assertThat(errors)
                .extracting(error -> Map.entry(error.getLocation(), error.getError()))
                .contains(
                        Map.entry("data.nameEnding", "Name ending is required"),
                        Map.entry("data.email", "Email is required"),
                        Map.entry("data.jurisdiction", "Jurisdiction is required"),
                        Map.entry("data.registeredOfficeAddress", "Registered office address is required"),
                        Map.entry("data.principalPlaceOfBusinessAddress", "Principal place of business address is required"),
                        Map.entry("data.lawfulPurposeStatementChecked", "Lawful purpose statement checked is required"));
    }

    @Test
    void shouldReturnErrorsWhenTermAndSicCodesAreMissingForPartnershipTypeThatRequiresThem() throws ServiceException {
        PartnershipDao partnershipDao = validRegistrationDao();
        partnershipDao.getData().setPartnershipType(PartnershipType.LP);
        partnershipDao.getData().setTerm(null);
        partnershipDao.getData().setSicCodes(null);

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(partnershipDao));

        List<ValidationStatusError> errors = partnershipService.validateStatus(transaction);

        assertThat(errors)
                .extracting(error -> Map.entry(error.getLocation(), error.getError()))
                .contains(
                        Map.entry("data.term", "Term is required"),
                        Map.entry("data.sicCodes", "SIC codes are required"));
    }

    @Test
    void shouldReturnErrorsWhenTermAndSicCodesAreProvidedForPartnershipTypeThatDoesNotAllowThem() throws ServiceException {
        PartnershipDao partnershipDao = validRegistrationDao();
        partnershipDao.getData().setPartnershipType(PartnershipType.PFLP);
        partnershipDao.getData().setTerm(Term.NONE);
        partnershipDao.getData().setSicCodes(List.of("12345"));

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(partnershipDao));

        List<ValidationStatusError> errors = partnershipService.validateStatus(transaction);

        assertThat(errors)
                .extracting(error -> Map.entry(error.getLocation(), error.getError()))
                .contains(
                        Map.entry("data.term", "Term is not required"),
                        Map.entry("data.sicCodes", "SIC codes are not required"));
    }

    @Test
    void shouldReturnPartnershipNumberErrorWhenTransitionPartnershipHasNoPartnershipNumber() throws ServiceException {
        Transaction transitionTransaction = new TransactionBuilder().withIncorporationKind(FilingMode.TRANSITION).build();
        PartnershipDao partnershipDao = validRegistrationDao();
        partnershipDao.getData().setPartnershipNumber(null);

        when(partnershipRepository.findByTransactionId(transitionTransaction.getId())).thenReturn(List.of(partnershipDao));

        List<ValidationStatusError> errors = partnershipService.validateStatus(transitionTransaction);

        assertThat(errors)
                .extracting(error -> Map.entry(error.getLocation(), error.getError()))
                .contains(Map.entry("data.partnershipNumber", "Partnership number is required"));
    }

    @Test
    void shouldFailValidateStatusWhenNoPartnershipFoundForTransaction() {
        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> partnershipService.validateStatus(transaction))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No limited partnership found for transaction id " + transaction.getId());
    }

    @Test
    void shouldFailValidateStatusWhenMoreThanOnePartnershipFound() {
        PartnershipDao first = validRegistrationDao();
        PartnershipDao second = new PartnershipBuilder().withId("another-submission-id").buildDao();

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> partnershipService.validateStatus(transaction))
                .isInstanceOf(ServiceException.class)
                .hasMessage("More than one limited partnership found for transaction id " + transaction.getId());
    }

    @Test
    void shouldFailValidateStatusWhenTransactionHasNoLimitedPartnershipResource() {
        Transaction transactionWithoutResource = new TransactionBuilder()
                .withKindAndUri("other-kind", URL_GET_PARTNERSHIP, SUBMISSION_ID)
                .build();

        when(partnershipRepository.findByTransactionId(transactionWithoutResource.getId()))
                .thenReturn(List.of(validRegistrationDao()));

        assertThatThrownBy(() -> partnershipService.validateStatus(transactionWithoutResource))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transaction id: " + transactionWithoutResource.getId() + " does not have a limited partnership resource");
    }

    private PartnershipDao validRegistrationDao() {
        return new PartnershipBuilder()
                .withId(SUBMISSION_ID)
                .withAddresses()
                .withPartnershipType(PartnershipType.LP)
                .withTerm(Term.NONE)
                .withSicCodes(List.of("12345"))
                .withLawfulPurposeStatementChecked(true)
                .buildDao();
    }
}

