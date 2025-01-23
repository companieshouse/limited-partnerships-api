package uk.gov.companieshouse.limitedpartnershipsapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_INCORPORATION;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.IncorporationDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipIncorporationDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipIncorporationRepository;

@ExtendWith(MockitoExtension.class)
class LimitedPartnershipIncorporationServiceTest {

    @InjectMocks
    LimitedPartnershipIncorporationService incorporationService;

    @Mock
    LimitedPartnershipIncorporationRepository repository;

    @Mock
    TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnershipIncorporationDao> incorporationCaptor;

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "12321123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";

    @Test
    void testCreateIncorporationIsSuccessful() throws ServiceException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnershipIncorporationDao limitedPartnershipIncorporationDao = createLimitedPartnershipIncorporationDao();
        when(repository.insert(any(LimitedPartnershipIncorporationDao.class))).thenReturn(limitedPartnershipIncorporationDao);

        // when
        var submissionId = incorporationService.createIncorporation(transaction, REQUEST_ID,
                USER_ID);

        // then
        verify(repository, times(1)).insert(incorporationCaptor.capture());
        assertEquals(SUBMISSION_ID, submissionId);

        LimitedPartnershipIncorporationDao sentSubmission = incorporationCaptor.getValue();
        IncorporationDataDao dataDao = sentSubmission.getData();
        assertEquals(FILING_KIND_REGISTRATION,dataDao.getKind());
        assertNotNull(dataDao.getEtag());
        // assert dao incorporation self link is correct
        String submissionUri = String.format(URL_GET_INCORPORATION, TRANSACTION_ID, submissionId);
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

    private LimitedPartnershipIncorporationDao createLimitedPartnershipIncorporationDao() {
        var dao = new LimitedPartnershipIncorporationDao();
        dao.setId(SUBMISSION_ID);
        dao.getData().setKind(FILING_KIND_REGISTRATION);
        dao.setCreatedAt(LocalDateTime.now());

        return dao;
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }
}
