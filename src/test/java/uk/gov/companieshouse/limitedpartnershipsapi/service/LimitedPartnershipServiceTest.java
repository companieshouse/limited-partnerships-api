package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
public class LimitedPartnershipServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String TRANSACTION_ID = "txn-456";
    private static final String LINK_SELF = "self";

    @InjectMocks
    private LimitedPartnershipService service;

    @Mock
    private LimitedPartnershipMapper mapper;

    @Mock
    private LimitedPartnershipSubmissionsRepository repository;

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionApiCaptor;

    @Captor
    private ArgumentCaptor<LimitedPartnershipSubmissionDao> submissionCaptor;

    @Test
    void givenDto_whenCreateLP_thenLPCreatedWithSubmissionIdAndTransactionUpdated() throws ServiceException {
        // given
        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();

        when(mapper.dtoToDao(limitedPartnershipSubmissionDto)).thenReturn(limitedPartnershipSubmissionDao);
        when(repository.insert(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDao);

        Transaction transaction = buildTransaction();
        
        // when
        String submissionId = service.createLimitedPartnership(transaction, limitedPartnershipSubmissionDto, REQUEST_ID, USER_ID);

        // then
        verify(mapper, times(1)).dtoToDao(limitedPartnershipSubmissionDto);
        verify(repository, times(1)).insert(limitedPartnershipSubmissionDao);
        verify(repository, times(1)).save(submissionCaptor.capture());
        verify(transactionService, times(1)).updateTransaction(transactionApiCaptor.capture(), any());
        assertEquals(SUBMISSION_ID, submissionId);

        // assert transaction resources are updated appropriately
        Transaction sentTransaction = transactionApiCaptor.getValue();
        assertEquals(limitedPartnershipSubmissionDto.getData().getPartnershipName(), sentTransaction.getCompanyName());
        assertNull(sentTransaction.getCompanyNumber());
        String submissionUri = String.format("/transactions/%s/limited-partnership/%s", transaction.getId(), limitedPartnershipSubmissionDao.getId());
        assertEquals(submissionUri, sentTransaction.getResources().get(submissionUri).getLinks().get("resource"));
        // assert dao submission self link is correct
        LimitedPartnershipSubmissionDao sentSubmission = submissionCaptor.getValue();
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

    @Test
    void givenTransactionAlreadyAssociatedWithAnLP_whenCreateLP_thenServiceExceptionThrown() throws ServiceException {
        // given
        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();

        Transaction transaction = buildTransaction();
        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        // when + then
        assertThrows(ServiceException.class, () -> service.createLimitedPartnership(transaction, limitedPartnershipSubmissionDto, REQUEST_ID, USER_ID));
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private LimitedPartnershipSubmissionDao createDao() {
        LimitedPartnershipSubmissionDao dao = new LimitedPartnershipSubmissionDao();
        dao.setId(SUBMISSION_ID);
        return dao;
    }

    private LimitedPartnershipSubmissionDto createDto() {
        var submissionDto = new LimitedPartnershipSubmissionDto();
        var dataDto = new DataDto();
        dataDto.setPartnershipName("Asset Strippers");
        dataDto.setNameEnding(PartnershipNameEnding.LP);
        submissionDto.setData(dataDto);

        return submissionDto;
    }
}
