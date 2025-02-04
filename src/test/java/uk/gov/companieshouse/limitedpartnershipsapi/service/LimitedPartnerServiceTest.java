package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.LINK_SELF;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
public class LimitedPartnerServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "12321123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    @InjectMocks
    LimitedPartnerService limitedPartnerService;
    @Mock
    LimitedPartnerRepository repository;
    @Mock
    TransactionService transactionService;
    @Captor
    private ArgumentCaptor<LimitedPartnerDao> limitedpartnerCaptor;
    @Mock
    private TransactionUtils transactionUtils;
    @Mock
    private LimitedPartnerMapper mapper;

    @Test
    void testCreateLimitedPartnerIsSuccessful() throws ServiceException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnerDao limitedpartnerDao = createLimitedPartnerDao();
        when(repository.insert(any(LimitedPartnerDao.class))).thenReturn(limitedpartnerDao);

        // when
        var submissionId = limitedPartnerService.createLimitedPartner(transaction, REQUEST_ID,
                USER_ID);

        // then
        verify(repository, times(1)).insert(limitedpartnerCaptor.capture());
        assertEquals(SUBMISSION_ID, submissionId);

        LimitedPartnerDao sentSubmission = limitedpartnerCaptor.getValue();
        LimitedPartnerDataDao dataDao = sentSubmission.getData();
        assertEquals(FILING_KIND_LIMITED_PARTNER, dataDao.getKind());
        assertNotNull(dataDao.getEtag());
        // assert dao limited partner self link is correct
        String submissionUri = String.format(URL_GET_LIMITED_PARTNER, TRANSACTION_ID, submissionId);
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);

        assertEquals(FILING_KIND_LIMITED_PARTNER, transaction.getFilingMode());
    }

    private LimitedPartnerDao createLimitedPartnerDao() {
        var dao = new LimitedPartnerDao();
        dao.setId(SUBMISSION_ID);
        dao.getData().setKind(FILING_KIND_LIMITED_PARTNER);
        dao.setCreatedAt(LocalDateTime.now());

        return dao;
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

}
