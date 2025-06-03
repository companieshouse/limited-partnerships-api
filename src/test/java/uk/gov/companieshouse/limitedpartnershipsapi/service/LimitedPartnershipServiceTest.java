package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.mapper.LimitedPartnershipPatchMapper;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.Jurisdiction;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dao.LimitedPartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.DataDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.model.partnership.dto.LimitedPartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.repository.LimitedPartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_RESUME_PARTNERSHIP;

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
    private LimitedPartnershipRepository repository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionUtils transactionUtils;

    @Mock
    private LimitedPartnershipValidator limitedPartnershipValidator;

    @Captor
    private ArgumentCaptor<Transaction> transactionApiCaptor;

    @Captor
    private ArgumentCaptor<LimitedPartnershipDao> submissionCaptor;

    @Test
    void givenDto_whenCreateLP_thenLPCreatedWithSubmissionIdAndTransactionUpdated() throws ServiceException {
        // given
        LimitedPartnershipDto limitedPartnershipDto = createDto();
        LimitedPartnershipDao limitedPartnershipDao = createDao();

        when(mapper.dtoToDao(limitedPartnershipDto)).thenReturn(limitedPartnershipDao);
        when(repository.insert(limitedPartnershipDao)).thenReturn(limitedPartnershipDao);

        Transaction transaction = buildTransaction();

        // when
        String submissionId = service.createLimitedPartnership(transaction, limitedPartnershipDto, REQUEST_ID, USER_ID);

        // then
        verify(mapper).dtoToDao(limitedPartnershipDto);
        verify(repository).insert(limitedPartnershipDao);
        verify(repository).save(submissionCaptor.capture());
        verify(transactionService).updateTransaction(transactionApiCaptor.capture(), any());
        assertEquals(SUBMISSION_ID, submissionId);

        // assert transaction resources are updated appropriately
        Transaction sentTransaction = transactionApiCaptor.getValue();
        assertEquals(limitedPartnershipDto.getData().getPartnershipName(), sentTransaction.getCompanyName());
        assertNull(sentTransaction.getCompanyNumber());
        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), limitedPartnershipDao.getId());
        assertEquals(submissionUri, sentTransaction.getResources().get(submissionUri).getLinks().get("resource"));
        // assert resume link is correct
        String resumeUri = String.format(URL_RESUME_PARTNERSHIP, transaction.getId(), limitedPartnershipDao.getId());
        assertEquals(resumeUri, sentTransaction.getResumeJourneyUri());
        // assert dao submission self link is correct
        LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

    @Test
    void givenTransactionAlreadyAssociatedWithAnLP_whenCreateLP_thenServiceExceptionThrown() {
        // given
        LimitedPartnershipDto limitedPartnershipDto = createDto();

        Transaction transaction = buildTransaction();
        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        // when + then
        assertThrows(ServiceException.class, () -> service.createLimitedPartnership(transaction,
                limitedPartnershipDto, REQUEST_ID, USER_ID));
    }

    @Test
    void giveInvalidSubmissionId_whenUpdateLp_ThenResourceNotFoundExceptionThrown() {
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
        LimitedPartnershipDao limitedPartnershipDao = createDao();
        var dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Strippers");
        dataDao.setNameEnding(PartnershipNameEnding.LP.getDescription());
        dataDao.setJurisdiction("Scotland");
        limitedPartnershipDao.setData(dataDao);
        limitedPartnershipDao.setCreatedBy("5fd36577288e");

        LimitedPartnershipDto limitedPartnershipDto = createDto();

        Transaction transaction = buildTransaction();
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();

        when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                limitedPartnershipDao));
        when(mapper.daoToDto(limitedPartnershipDao)).thenReturn(limitedPartnershipDto);
        LimitedPartnershipDao limitedPartnershipDaoAfterPatch = createDao();
        when(mapper.dtoToDao(limitedPartnershipDto)).thenReturn(
                limitedPartnershipDaoAfterPatch);

        // when
        service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

        // then
        verify(repository).findById(SUBMISSION_ID);
        verify(repository).save(submissionCaptor.capture());

        LimitedPartnershipDao sentSubmission = submissionCaptor.getValue();
        assertEquals("5fd36577288e", sentSubmission.getCreatedBy());
        assertEquals(USER_ID, sentSubmission.getUpdatedBy());

        verify(transactionService).updateTransactionWithPartnershipName(transaction, REQUEST_ID, "Asset Adders");
    }

    @Test
    void givenWrongSubmissionId_whenUpdateLP_thenServiceExceptionThrown() {
        // given
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        Transaction transaction = buildTransaction();
        var limitedPartnershipPatchDto = new LimitedPartnershipPatchDto();

        // when + then
        assertThrows(ServiceException.class, () -> service.updateLimitedPartnership(
                transaction, "wrong-id", limitedPartnershipPatchDto, REQUEST_ID, USER_ID));
    }

    @Test
    void giveSubmissionId_whenGetLp_ThenLPRetrieved() throws ResourceNotFoundException {
        // given
        LimitedPartnershipDto limitedPartnershipDto = createDto();
        LimitedPartnershipDao limitedPartnershipDao = createDao();
        Transaction transaction = buildTransaction();

        when(transactionUtils.isTransactionLinkedToLimitedPartnership(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(limitedPartnershipDao.getId())).thenReturn(Optional.of(
                limitedPartnershipDao));
        when(mapper.daoToDto(limitedPartnershipDao)).thenReturn(limitedPartnershipDto);

        // when
        LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository).findById(limitedPartnershipDao.getId());
        verify(mapper).daoToDto(limitedPartnershipDao);
        assertEquals(limitedPartnershipDto.getData(), retrievedDto.getData());
    }

    @Test
    void giveInvalidSubmissionId_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnership(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction, "wrong-id"));
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.isTransactionLinkedToLimitedPartnership(eq(transaction), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction, SUBMISSION_ID));
    }

    @Test
    void givenTransactionId_whenGetLp_ThenLPRetrieved() throws ServiceException {
        // given
        LimitedPartnershipDto limitedPartnershipDto = createDto();
        LimitedPartnershipDao limitedPartnershipDao = createDao();
        Transaction transaction = buildTransaction();

        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(
                limitedPartnershipDao));
        when(mapper.daoToDto(limitedPartnershipDao)).thenReturn(limitedPartnershipDto);

        // when
        LimitedPartnershipDto retrievedDto = service.getLimitedPartnership(transaction);

        // then
        verify(repository).findByTransactionId(transaction.getId());
        verify(mapper).daoToDto(limitedPartnershipDao);
        assertEquals(limitedPartnershipDto.getData(), retrievedDto.getData());
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
    void givenTransactionIdHasNoLpSubmission_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    void givenTransactionIdHasMultipleLpSubmissions_whenGetLp_ThenServiceExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();
        LimitedPartnershipDao lpDao1 = createDao();
        LimitedPartnershipDao lpDao2 = createDao();

        when(transactionUtils.doesTransactionHaveALimitedPartnershipSubmission(transaction)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(lpDao1, lpDao2));

        // when + then
        assertThrows(ServiceException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    @Disabled
    void givenNoErrorsWithPartnershipData_whenValidateStatus_thenNoErrorsReturned() throws ServiceException {
        // given
        LimitedPartnershipDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao();
        Transaction transaction = buildTransaction();

        when(transactionUtils.isTransactionLinkedToLimitedPartnership(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);
        when(limitedPartnershipValidator.validate(limitedPartnershipSubmissionDto)).thenReturn(new ArrayList<>());

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(0, results.size());
    }

    @Test
    @Disabled
    void givenErrorsWithPartnershipData_whenValidateStatus_thenErrorsReturned() throws ServiceException {
        // given
        LimitedPartnershipDto limitedPartnershipSubmissionDto = createDto();
        LimitedPartnershipDao limitedPartnershipSubmissionDao = createDao();
        Transaction transaction = buildTransaction();

        when(transactionUtils.isTransactionLinkedToLimitedPartnership(eq(transaction), any(String.class))).thenReturn(true);
        when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);
        List<ValidationStatusError> errorsList = new ArrayList<>();
        var error1 = new ValidationStatusError("Missing field", "here", null, null);
        var error2 = new ValidationStatusError("Invalid data format", "there", null, null);
        errorsList.add(error1);
        errorsList.add(error2);
        when(limitedPartnershipValidator.validate(limitedPartnershipSubmissionDto)).thenReturn(errorsList);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(2, results.size());
        assertThat(results, hasItems(error1, error2));
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenValidateStatus_ThenResourceNotFoundExceptionThrown() {
        // given
        Transaction transaction = buildTransaction();

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.validateLimitedPartnership(transaction));
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private LimitedPartnershipDao createDao() {
        LimitedPartnershipDao dao = new LimitedPartnershipDao();
        dao.setId(SUBMISSION_ID);
        DataDao dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Adders");
        dataDao.setJurisdiction("Scotland");
        dao.setData(dataDao);
        return dao;
    }

    private LimitedPartnershipDto createDto() {
        var submissionDto = new LimitedPartnershipDto();
        var dataDto = new DataDto();
        dataDto.setPartnershipName("Asset Strippers");
        dataDto.setNameEnding(PartnershipNameEnding.LP);
        dataDto.setJurisdiction(Jurisdiction.SCOTLAND);
        submissionDto.setData(dataDto);

        return submissionDto;
    }
}
