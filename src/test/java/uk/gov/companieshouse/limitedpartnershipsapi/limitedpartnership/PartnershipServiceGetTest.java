package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceGetTest extends AbstractPartnershipServiceTestSupport {

    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

    private final Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private PartnershipService partnershipService;

    @MockitoBean
    private PartnershipRepository partnershipRepository;

    @Captor
    private ArgumentCaptor<String> submissionIdCaptor;

    @Captor
    private ArgumentCaptor<String> transactionIdCaptor;

    @Test
    void shouldGetPartnershipBySubmissionId() throws ResourceNotFoundException {
        PartnershipDao partnershipDao = new PartnershipBuilder().withId(SUBMISSION_ID).buildDao();

        when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

        PartnershipDto retrievedDto = partnershipService.getLimitedPartnership(transaction, SUBMISSION_ID);

        verify(partnershipRepository).findById(submissionIdCaptor.capture());
        assertThat(submissionIdCaptor.getValue()).isEqualTo(SUBMISSION_ID);

        assertThat(retrievedDto.getData())
                .usingRecursiveComparison()
                .ignoringFields("companyPreviousDetails")
                .isEqualTo(partnershipDao.getData());
    }

    @Test
    void shouldFailGetPartnershipBySubmissionIdWhenNotFound() {
        when(partnershipRepository.findById("wrong-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> partnershipService.getLimitedPartnership(transaction, "wrong-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Limited Partnership with id wrong-id not found");
    }

    @Test
    void shouldFailGetPartnershipBySubmissionIdWhenNotLinkedToTransaction() {
        PartnershipDao partnershipDao = new PartnershipBuilder().withId(SUBMISSION_ID).buildDao();
        Transaction differentTransaction = new TransactionBuilder()
                .withResource(String.format(URL_GET_PARTNERSHIP, transaction.getId(), "different-submission-id"))
                .build();

        when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDao));

        assertThatThrownBy(() -> partnershipService.getLimitedPartnership(differentTransaction, SUBMISSION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transaction id: 863851-951242-143528 does not have a resource that matches submission id: " + SUBMISSION_ID);
    }

    @Test
    void shouldGetPartnershipByTransactionId() throws ServiceException {
        PartnershipDao partnershipDao = new PartnershipBuilder().withId(SUBMISSION_ID).buildDao();

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(partnershipDao));

        PartnershipDto retrievedDto = partnershipService.getLimitedPartnership(transaction);

        verify(partnershipRepository).findByTransactionId(transactionIdCaptor.capture());
        assertThat(transactionIdCaptor.getValue()).isEqualTo(transaction.getId());

        assertThat(retrievedDto.getData())
                .usingRecursiveComparison()
                .ignoringFields("companyPreviousDetails")
                .isEqualTo(partnershipDao.getData());
    }

    @Test
    void shouldFailGetPartnershipByTransactionIdWhenNotFound() {
        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> partnershipService.getLimitedPartnership(transaction))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No limited partnership found for transaction id " + transaction.getId());
    }

    @Test
    void shouldFailGetPartnershipByTransactionIdWhenTransactionHasNoLinkedPartnershipResource() {
        PartnershipDao partnershipDao = new PartnershipBuilder().withId(SUBMISSION_ID).buildDao();
        Transaction transactionWithoutLimitedPartnershipResource = new TransactionBuilder()
                .withKindAndUri("other-kind", URL_GET_PARTNERSHIP, SUBMISSION_ID)
                .build();

        when(partnershipRepository.findByTransactionId(transactionWithoutLimitedPartnershipResource.getId()))
                .thenReturn(List.of(partnershipDao));

        assertThatThrownBy(() -> partnershipService.getLimitedPartnership(transactionWithoutLimitedPartnershipResource))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transaction id: " + transactionWithoutLimitedPartnershipResource.getId() + " does not have a limited partnership resource");
    }

    @Test
    void shouldFailGetPartnershipByTransactionIdWhenMoreThanOnePartnershipFound() {
        PartnershipDao firstPartnershipDao = new PartnershipBuilder().withId(SUBMISSION_ID).buildDao();
        PartnershipDao secondPartnershipDao = new PartnershipBuilder().withId("another-submission-id").buildDao();

        when(partnershipRepository.findByTransactionId(transaction.getId())).thenReturn(List.of(firstPartnershipDao, secondPartnershipDao));

        assertThatThrownBy(() -> partnershipService.getLimitedPartnership(transaction))
                .isInstanceOf(ServiceException.class)
                .hasMessage("More than one limited partnership found for transaction id " + transaction.getId());
    }
}
