package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PscMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PscRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_PSC;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PSC;

@ExtendWith(MockitoExtension.class)
class PscServiceTest {

    private static final String REQUEST_ID = "request123";
    private static final String USER_ID = "user123";
    private static final String SUBMISSION_ID = PscBuilder.ID;

    Transaction transaction = new TransactionBuilder().withKindAndUri(
                    FILING_KIND_PSC,
                    URL_GET_PSC,
                    SUBMISSION_ID
            )
            .build();

    @InjectMocks
    private PscService pscService;

    @Mock
    private PscRepository repository;

    @Mock
    private PscMapper mapper;

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<PscDao> submissionCaptor;

    @Test
    void testGetPscSuccess() throws ServiceException {
        PscDao dao = PscBuilder.PscPersonDao();

        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.of(dao));

        when(mapper.daoToDto(dao)).thenReturn(PscBuilder.personPscDto());
        when(transactionService.isTransactionLinkedToResource(any(), anyString(), anyString()))
                .thenReturn(true);

        var dto = pscService.getPsc(transaction, SUBMISSION_ID);
        assertEquals("John", dto.getData().getForename());
        assertEquals("Smith", dto.getData().getSurname());
    }

    @Test
    void testGetPscNotFound() {
        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> pscService.getPsc(transaction, SUBMISSION_ID));
        assertEquals("Person with significant control resource with id " + SUBMISSION_ID + " not found", resourceNotFoundException.getMessage());
    }

    @Test
    void testTransactionLinkedToPscFails() {
        PscDao dao = PscBuilder.PscPersonDao();

        when(repository.findById(SUBMISSION_ID))
                .thenReturn(Optional.of(dao));

        when(transactionService.isTransactionLinkedToResource(eq(transaction), any(String.class), any(String.class)))
                .thenReturn(false);
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> pscService.getPsc(transaction, SUBMISSION_ID));
        assertEquals(String.format("Transaction id: %s does not have a resource that matches person with significant control id: %s", transaction.getId(), SUBMISSION_ID), resourceNotFoundException.getMessage());
    }

    @Test
    void testCreatePscReturnsSuccess() throws ServiceException {
        var submissionUri = String.format(URL_GET_PSC, transaction.getId(), SUBMISSION_ID);
        PscDto dto =  PscBuilder.getPscDto();
        PscDao dao = PscBuilder.getPscDao();

        when(mapper.dtoToDao(dto)).thenReturn(dao);
        when(repository.insert(dao)).thenReturn(dao);

        String submissionId = pscService.createPsc(transaction, dto, REQUEST_ID, USER_ID);

        verify(mapper, times(1)).dtoToDao(dto);
        verify(repository, times(1)).insert(dao);
        verify(repository, times(1)).save(submissionCaptor.capture());
        verify(transactionService, times(1)).updateTransactionWithLinksForResource(REQUEST_ID, transaction, submissionUri, dao.getData().getKind(), null);

        PscDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_PSC, sentSubmission.getData().getKind());
        assertEquals(SUBMISSION_ID, submissionId);

        // Assert self link
        String expectedUri = String.format(URL_GET_PSC, transaction.getId(), SUBMISSION_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));

    }
}
