package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnerMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.LimitedPartnerType;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnerDataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnerDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnerRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNER;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_LIMITED_PARTNER;

@ExtendWith(MockitoExtension.class)
class LimitedPartnerServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String SUBMISSION_ID = "abc-123";
    private static final String TRANSACTION_ID = "12321123";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    @InjectMocks
    LimitedPartnerService service;

    @Mock
    LimitedPartnerRepository repository;

    //@Mock
    TransactionService transactionService;

    @Captor
    private ArgumentCaptor<LimitedPartnerDao> submissionCaptor;

    @Mock
    private TransactionUtils transactionUtils;

    @Mock
    private LimitedPartnerMapper mapper;

    @Mock
    private LimitedPartnerType limitedPartnerType;

    @Test
    void testCreateLimitedPartnerIsSuccessful() throws ServiceException {
        // given
        LimitedPartnerDto limitedPartnerDto = createDto();
        LimitedPartnerDao limitedPartnerDao = createDao();
        limitedPartnerDao.setId(SUBMISSION_ID);

        when(mapper.dtoToDao(limitedPartnerDto)).thenReturn(limitedPartnerDao);
        when(repository.insert(limitedPartnerDao)).thenReturn(limitedPartnerDao);

        Transaction transaction = buildTransaction();

        // when
        String submissionId = service.createLimitedPartner(transaction, limitedPartnerDto, REQUEST_ID, USER_ID);

        // then
        verify(mapper, times(1)).dtoToDao(limitedPartnerDto);
        verify(repository, times(1)).insert(limitedPartnerDao);
        verify(repository, times(1)).save(submissionCaptor.capture());

        LimitedPartnerDao sentSubmission = submissionCaptor.getValue();
        assertEquals(USER_ID, sentSubmission.getCreatedBy());
        assertEquals(FILING_KIND_LIMITED_PARTNER, sentSubmission.getData().getKind());
        assertEquals(SUBMISSION_ID, submissionId);
        assertEquals(LimitedPartnerType.LEGAL_ENTITY, sentSubmission.getData().getPartnerType());

        // Assert self link
        String expectedUri = String.format(URL_GET_LIMITED_PARTNER, transaction.getId(), SUBMISSION_ID);
        assertEquals(expectedUri, sentSubmission.getLinks().get("self"));
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenGetLp_ThenResourceNotFoundExceptionThrown() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartner(transaction, SUBMISSION_ID));
    }

    @Test
    void givenTransactionNotLinkedToSubmission_whenGetLimitedPartner_thenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        String submissionId = "submissionId";
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartner(transaction, submissionId));
    }

    @Test
    void givenSubmissionNotFound_whenGetLimitedPartner_thenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        String submissionId = "submissionId";
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(submissionId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartner(transaction, submissionId));
    }

    @Test
    void givenSubmissionFound_whenGetLimitedPartner_thenLimitedPartnerRetrieved() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        String submissionId = "submissionId";
        LimitedPartnerDao limitedPartnerDao = createDao();
        LimitedPartnerDto limitedPartnerDto = createDto();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(submissionId)).thenReturn(Optional.of(limitedPartnerDao));
        when(mapper.daoToDto(limitedPartnerDao)).thenReturn(limitedPartnerDto);

        // when
        LimitedPartnerDto result = service.getLimitedPartner(transaction, submissionId);

        // then
        assertEquals(limitedPartnerDto, result);
    }

    @Test
    void givenTransactionWithoutLimitedPartnershipSubmission_whenGetLimitedPartner_thenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartner(transaction));
    }

    @Test
    void givenNoLimitedPartnershipFound_whenGetLimitedPartner_thenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(Collections.emptyList());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartner(transaction));
    }

    @Test
    void givenMultipleLimitedPartnershipsFound_whenGetLimitedPartner_thenServiceExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnerDao limitedPartnerDao1 = createDao();
        LimitedPartnerDao limitedPartnerDao2 = createDao();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(Arrays.asList(limitedPartnerDao1, limitedPartnerDao2));

        // when + then
        assertThrows(ServiceException.class, () -> service.getLimitedPartner(transaction));
    }

    @Test
    void givenOneLimitedPartnershipFound_whenGetLimitedPartner_thenLimitedPartnerRetrieved() throws ResourceNotFoundException, ServiceException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnerDao limitedPartnerDao = createDao();
        LimitedPartnerDto limitedPartnerDto = createDto();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(Collections.singletonList(limitedPartnerDao));
        when(mapper.daoToDto(limitedPartnerDao)).thenReturn(limitedPartnerDto);

        // when
        LimitedPartnerDto result = service.getLimitedPartner(transaction);

        // then
        assertEquals(limitedPartnerDto, result);
    }

    private LimitedPartnerDto createDto() {
        LimitedPartnerDto dto = new LimitedPartnerDto();
        LimitedPartnerDataDto dataDto = new LimitedPartnerDataDto();
        dataDto.setPartnerType(LimitedPartnerType.LEGAL_ENTITY);

        return dto;
    }

    private LimitedPartnerDao createDao() {
        LimitedPartnerDao dao = new LimitedPartnerDao();
        LimitedPartnerDataDao dataDao = new LimitedPartnerDataDao();
        dataDao.setPartnerType(LimitedPartnerType.LEGAL_ENTITY);
        dao.setData(dataDao);
        return dao;
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        return transaction;
    }

    private boolean hasExistingLimitedPartnerSubmission(Transaction transaction) {
        if (transaction.getResources() != null) {
            return transaction.getResources().entrySet().stream().anyMatch(
                    resourceEntry -> FILING_KIND_LIMITED_PARTNER.equals(resourceEntry.getValue().getKind()));
        }
        return false;
    }

}
