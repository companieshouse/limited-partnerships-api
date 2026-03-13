package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PscBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.PscMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dao.PscDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.psc.dto.PscDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.PscRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testCreatePscReturnsSuccess() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
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
