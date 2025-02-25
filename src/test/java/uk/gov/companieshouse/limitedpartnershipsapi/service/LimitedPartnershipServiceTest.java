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
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipPatchMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dao.LimitedPartnershipSubmissionDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.LimitedPartnershipSubmissionDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipSubmissionsRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.TransactionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.dto.validator.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
class LimitedPartnershipServiceTest {

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
    private LimitedPartnershipPatchMapper patchMapper;

    @Mock
    private LimitedPartnershipSubmissionsRepository repository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionUtils transactionUtils;

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
        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), limitedPartnershipSubmissionDao.getId());
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

    @Test
    void giveInvalidSubmissionId_whenUpdateLp_ThenResourceNotFoundExceptionThrown() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.updateLimitedPartnership(transaction, "wrong-id", limitedPartnershipPatchDto, REQUEST_ID, USER_ID));
    }

    @Test
    void givenData_whenUpdateLP_thenLPSubmissionUpdated() throws ServiceException {
        // given
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();
        var dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Strippers");
        dataDao.setNameEnding(PartnershipNameEnding.LP.getDescription());
        dataDao.setJurisdiction("Scotland");
        limitedPartnershipSubmissionDao.setData(dataDao);
        limitedPartnershipSubmissionDao.setCreatedBy("5fd36577288e");

        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();

        Transaction transaction = buildTransaction();
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();

        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDaoAfterPatch = createDao();
        when(mapper.dtoToDao(limitedPartnershipSubmissionDto)).thenReturn(limitedPartnershipSubmissionDaoAfterPatch);

        // when
        service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

        // then
        verify(repository, times(1)).findById(SUBMISSION_ID);
        verify(repository, times(1)).save(submissionCaptor.capture());

        LimitedPartnershipSubmissionDao sentSubmission = submissionCaptor.getValue();
        assertEquals("5fd36577288e", sentSubmission.getCreatedBy());
        assertEquals(USER_ID, sentSubmission.getUpdatedBy());

        verify(transactionService, times(1)).updateTransactionWithPartnershipName(transaction, REQUEST_ID, "Asset Adders");
    }

    @Test
    void givenWrongSubmissionId_whenUpdateLP_thenServiceExceptionThrown() throws ServiceException {
        // given
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        Map<String, Object> data = new HashMap<>();
        data.put("email", "test@email.com");

        Transaction transaction = buildTransaction();
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();

        // when + then
        assertThrows(ServiceException.class, () -> service.updateLimitedPartnership(
                transaction, "wrong-id", limitedPartnershipPatchDto, REQUEST_ID, USER_ID));
    }

    @Test
    void giveSubmissionId_whenGetLp_ThenLPRetrieved() throws ResourceNotFoundException {
        // given
        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();
        Transaction transaction = buildTransaction();

        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(limitedPartnershipSubmissionDao.getId())).thenReturn(Optional.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);

        // when
        LimitedPartnershipSubmissionDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository, times(1)).findById(limitedPartnershipSubmissionDao.getId());
        verify(mapper, times(1)).daoToDto(limitedPartnershipSubmissionDao);
        assertEquals(limitedPartnershipSubmissionDto.getData(), retrievedDto.getData());
    }

    @Test
    void giveInvalidSubmissionId_whenGetLp_ThenResourceNotFoundExceptionThrown() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction, "wrong-id"));
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenGetLp_ThenResourceNotFoundExceptionThrown() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnershipSubmission(eq(transaction), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction, SUBMISSION_ID));
    }

    @Test
    void givenTransactionId_whenGetLp_ThenLPRetrieved() throws ServiceException {
        // given
        LimitedPartnershipSubmissionDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipSubmissionDao limitedPartnershipSubmissionDao = createDao();
        Transaction transaction = buildTransaction();

        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);

        // when
        LimitedPartnershipSubmissionDto retrievedDto = service.getLimitedPartnership(transaction);

        // then
        verify(repository, times(1)).findByTransactionId(transaction.getId());
        verify(mapper, times(1)).daoToDto(limitedPartnershipSubmissionDao);
        assertEquals(limitedPartnershipSubmissionDto.getData(), retrievedDto.getData());
    }

    @Test
    void givenInvalidTransactionId_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(Collections.emptyList());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    void givenTransactionIdHasNoLpSubmission_whenGetLp_ThenResourceNotFoundExceptionThrown() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    void givenTransactionIdHasMultipleLpSubmissions_whenGetLp_ThenServiceExceptionThrown() throws ResourceNotFoundException {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnershipSubmissionDao lpDao1 = createDao();
        LimitedPartnershipSubmissionDao lpDao2 = createDao();

        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(lpDao1, lpDao2));

        // when + then
        assertThrows(ServiceException.class, () -> service.getLimitedPartnership(transaction));
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private LimitedPartnershipSubmissionDao createDao() {
        LimitedPartnershipSubmissionDao dao = new LimitedPartnershipSubmissionDao();
        dao.setId(SUBMISSION_ID);
        DataDao dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Adders");
        dataDao.setJurisdiction("Scotland");
        dao.setData(dataDao);
        return dao;
    }

    private LimitedPartnershipSubmissionDto createDto() {
        var submissionDto = new LimitedPartnershipSubmissionDto();
        var dataDto = new DataDto();
        dataDto.setPartnershipName("Asset Strippers");
        dataDto.setNameEnding(PartnershipNameEnding.LP);
        dataDto.setJurisdiction(Jurisdiction.SCOTLAND);
        submissionDto.setData(dataDto);

        return submissionDto;
    }
}
