package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.DataDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.enums.PartnershipNameEnding;
import uk.gov.companieshouse.limitedpartnershipsapi.service.TransactionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.model.common.FilingMode.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
class PartnershipServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String LINK_SELF = "self";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    Transaction transaction = new TransactionBuilder().build();

    @InjectMocks
    private PartnershipService service;

    @Mock
    private PartnershipMapper mapper;

    @Mock
    private PartnershipPatchMapper patchMapper;

    @Mock
    private PartnershipRepository repository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private LimitedPartnershipValidator limitedPartnershipValidator;

    @Captor
    private ArgumentCaptor<PartnershipDao> submissionCaptor;

    @Test
    void givenDto_whenCreateLP_thenLPCreatedWithSubmissionIdAndTransactionUpdated() throws Exception {
        // given
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

        when(mapper.dtoToDao(partnershipDto)).thenReturn(partnershipDao);
        when(repository.insert(partnershipDao)).thenReturn(partnershipDao);

        // when
        String submissionId = service.createLimitedPartnership(transaction, partnershipDto, REQUEST_ID, USER_ID);

        // then
        verify(mapper).dtoToDao(partnershipDto);
        verify(repository).insert(partnershipDao);
        verify(repository).save(submissionCaptor.capture());
        verify(transactionService).updateTransactionWithLinksAndPartnershipName(
                eq(transaction),
                any(),
                any(),
                any(),
                any(),
                any());
        assertEquals(SUBMISSION_ID, submissionId);

        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), partnershipDao.getId());
        PartnershipDao sentSubmission = submissionCaptor.getValue();
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

    @Test
    void givenTransactionAlreadyAssociatedWithAnLP_whenCreateLP_thenServiceExceptionThrown() {
        // given
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        when(transactionService.hasExistingLimitedPartnership(transaction)).thenReturn(true);

        Resource resource = new Resource();
        resource.setKind(FILING_KIND_LIMITED_PARTNERSHIP);
        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put(String.format("/transactions/%s/limited-partnership/%s", TRANSACTION_ID, SUBMISSION_ID), resource);
        transaction.setResources(resourceMap);

        // when + then
        assertThrows(ServiceException.class, () -> service.createLimitedPartnership(transaction,
            partnershipDto, REQUEST_ID, USER_ID));
    }

    @Test
    void giveInvalidSubmissionId_whenUpdateLp_ThenResourceNotFoundExceptionThrown() {
        // given
        var limitedPartnershipPatchDto = new PartnershipPatchDto();
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.updateLimitedPartnership(transaction, "wrong-id", limitedPartnershipPatchDto, REQUEST_ID, USER_ID));
    }

    @Test
    void givenData_whenUpdateLP_thenLPSubmissionUpdated() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
        var dataDao = new DataDao();
        dataDao.setPartnershipName("Asset Strippers");
        dataDao.setNameEnding(PartnershipNameEnding.LP.getDescription());
        dataDao.setJurisdiction("Scotland");
        partnershipDao.setData(dataDao);
        partnershipDao.setCreatedBy("5fd36577288e");

        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        var limitedPartnershipPatchDto = new PartnershipPatchDto();

        when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
            partnershipDao));
        when(mapper.daoToDto(partnershipDao)).thenReturn(partnershipDto);
        PartnershipDao partnershipDaoAfterPatch = new PartnershipBuilder().buildDao();
        when(mapper.dtoToDao(partnershipDto)).thenReturn(
            partnershipDaoAfterPatch);
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // when
        service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

        // then
        verify(repository).findById(SUBMISSION_ID);
        verify(repository).save(submissionCaptor.capture());

        PartnershipDao sentSubmission = submissionCaptor.getValue();
        assertEquals("5fd36577288e", sentSubmission.getCreatedBy());
        assertEquals(USER_ID, sentSubmission.getUpdatedBy());

        verify(transactionService).updateTransactionWithPartnershipName(transaction, REQUEST_ID, partnershipDto.getData().getPartnershipName());
    }

    @Test
    void givenWrongSubmissionId_whenUpdateLP_thenServiceExceptionThrown() {
        // given
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        var limitedPartnershipPatchDto = new PartnershipPatchDto();

        // when + then
        assertThrows(ServiceException.class, () -> service.updateLimitedPartnership(
                transaction, "wrong-id", limitedPartnershipPatchDto, REQUEST_ID, USER_ID));
    }

    @Test
    void giveSubmissionId_whenGetLp_ThenLPRetrieved() throws ResourceNotFoundException {
        // given
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

        when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
            partnershipDao));
        when(transactionService.isTransactionLinkedToResource(eq(transaction), any(String.class), eq(FILING_KIND_LIMITED_PARTNERSHIP))).thenReturn(true);
        when(mapper.daoToDto(partnershipDao)).thenReturn(partnershipDto);

        // when
        PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        verify(repository).findById(partnershipDao.getId());
        verify(mapper).daoToDto(partnershipDao);
        assertEquals(partnershipDto.getData(), retrievedDto.getData());
    }

    @Test
    void giveInvalidSubmissionId_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        when(repository.findById("wrong-id")).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction, "wrong-id"));
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
        when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
            partnershipDao));
        when(transactionService.isTransactionLinkedToResource(eq(transaction), any(String.class), any(String.class))).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction, SUBMISSION_ID));
    }

    @Test
    void givenTransactionId_whenGetLp_ThenLPRetrieved() throws ServiceException {
        // given
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

        when(transactionService.doesTransactionHaveALimitedPartnership(transaction, FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(
            partnershipDao));
        when(mapper.daoToDto(partnershipDao)).thenReturn(partnershipDto);

        // when
        PartnershipDto retrievedDto = service.getLimitedPartnership(transaction);

        // then
        verify(repository).findByTransactionId(transaction.getId());
        verify(mapper).daoToDto(partnershipDao);
        assertEquals(partnershipDto.getData(), retrievedDto.getData());
    }

    @Test
    void givenInvalidTransactionId_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        when(repository.findByTransactionId(transaction.getId())).thenReturn(Collections.emptyList());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    void givenTransactionIdHasNoLpSubmission_whenGetLp_ThenResourceNotFoundExceptionThrown() {
        // given
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(
            partnershipDao));
        when(transactionService.doesTransactionHaveALimitedPartnership(transaction, FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(false);

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    void givenTransactionIdHasMultipleLpSubmissions_whenGetLp_ThenServiceExceptionThrown() {
        // given
        PartnershipDao lpDao1 = new PartnershipBuilder().buildDao();
        PartnershipDao lpDao2 = new PartnershipBuilder().buildDao();

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(lpDao1, lpDao2));

        // when + then
        assertThrows(ServiceException.class, () -> service.getLimitedPartnership(transaction));
    }

    @Test
    void givenNoErrorsWithPartnershipData_whenValidateStatus_thenNoErrorsReturned() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        PartnershipDto limitedPartnershipSubmissionDto = new PartnershipBuilder().buildDto();
        PartnershipDao limitedPartnershipSubmissionDao = new PartnershipBuilder().buildDao();

        when(transactionService.doesTransactionHaveALimitedPartnership(transaction, FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(true);
        when(repository.findByTransactionId(TRANSACTION_ID)).thenReturn(List.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);
        when(limitedPartnershipValidator.validateFull(limitedPartnershipSubmissionDto, REGISTRATION)).thenReturn(new ArrayList<>());

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(0, results.size());
    }

    @Test
    void givenErrorsWithPartnershipData_whenValidateStatus_thenErrorsReturned() throws ServiceException, MethodArgumentNotValidException, NoSuchMethodException {
        // given
        PartnershipDto limitedPartnershipSubmissionDto = new PartnershipBuilder().buildDto();
        PartnershipDao limitedPartnershipSubmissionDao = new PartnershipBuilder().buildDao();

        when(transactionService.doesTransactionHaveALimitedPartnership(transaction, FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(true);
        when(repository.findByTransactionId(TRANSACTION_ID)).thenReturn(List.of(limitedPartnershipSubmissionDao));
        when(mapper.daoToDto(limitedPartnershipSubmissionDao)).thenReturn(limitedPartnershipSubmissionDto);
        List<ValidationStatusError> errorsList = new ArrayList<>();
        var error1 = new ValidationStatusError("Missing field", "here", null, null);
        var error2 = new ValidationStatusError("Invalid data format", "there", null, null);
        errorsList.add(error1);
        errorsList.add(error2);
        when(limitedPartnershipValidator.validateFull(limitedPartnershipSubmissionDto, REGISTRATION)).thenReturn(errorsList);

        // when
        List<ValidationStatusError> results = service.validateLimitedPartnership(transaction);

        // then
        assertEquals(2, results.size());
        assertThat(results, hasItems(error1, error2));
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenValidateStatus_ThenResourceNotFoundExceptionThrown() {
        assertThrows(ResourceNotFoundException.class, () -> service.validateLimitedPartnership(transaction));
    }

    @Nested
    class Transactional {
        @Test
        void givenTransactionUpdateFails_whenCreateLP_thenInsertedLPIsDeleted() throws Exception {
            // given
            PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
            PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

            when(mapper.dtoToDao(partnershipDto)).thenReturn(partnershipDao);
            when(repository.insert(partnershipDao)).thenReturn(partnershipDao);
            doThrow(new ServiceException("Transaction update failed"))
                .when(transactionService).updateTransactionWithLinksAndPartnershipName(
                    eq(transaction), any(), any(), any(), any(), any());

            // when + then
            assertThrows(ServiceException.class,
                () -> service.createLimitedPartnership(transaction, partnershipDto, REQUEST_ID, USER_ID));

            verify(repository).deleteById(SUBMISSION_ID);
        }

        @Test
        void givenTransactionUpdateSucceeds_whenCreateLP_thenInsertedLPIsNotDeleted() throws Exception {
            // given
            PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
            PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

            when(mapper.dtoToDao(partnershipDto)).thenReturn(partnershipDao);
            when(repository.insert(partnershipDao)).thenReturn(partnershipDao);

            // when
            service.createLimitedPartnership(transaction, partnershipDto, REQUEST_ID, USER_ID);

            // then
            verify(repository, never()).deleteById(any());
        }

        @Test
        void givenTransactionUpdateFails_whenUpdateLP_thenOriginalLPIsRestored() throws Exception {
            // given
            PartnershipDao partnershipDaoBeforePatch = new PartnershipBuilder().buildDao();
            partnershipDaoBeforePatch.setCreatedBy("original-creator");

            PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
            PartnershipDao partnershipDaoAfterPatch = new PartnershipBuilder().buildDao();

            when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDaoBeforePatch));
            when(mapper.daoToDto(partnershipDaoBeforePatch)).thenReturn(partnershipDto);
            when(mapper.dtoToDao(partnershipDto)).thenReturn(partnershipDaoAfterPatch);
            when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);
            doThrow(new ServiceException("Transaction update failed"))
                .when(transactionService).updateTransactionWithPartnershipName(any(), any(), any());

            // when + then
            assertThrows(ServiceException.class,
                () -> service.updateLimitedPartnership(transaction, SUBMISSION_ID, new PartnershipPatchDto(), REQUEST_ID, USER_ID));

            // verify: first save = patched DAO, second save = original DAO (rollback)
            verify(repository, times(2)).save(submissionCaptor.capture());
            List<PartnershipDao> savedValues = submissionCaptor.getAllValues();
            assertEquals(partnershipDaoAfterPatch, savedValues.get(0));
            assertEquals(partnershipDaoBeforePatch, savedValues.get(1));
        }

        @Test
        void givenTransactionUpdateSucceeds_whenUpdateLP_thenOriginalLPIsNotRestored() throws Exception {
            // given
            PartnershipDao partnershipDaoBeforePatch = new PartnershipBuilder().buildDao();
            PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
            PartnershipDao partnershipDaoAfterPatch = new PartnershipBuilder().buildDao();

            when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDaoBeforePatch));
            when(mapper.daoToDto(partnershipDaoBeforePatch)).thenReturn(partnershipDto);
            when(mapper.dtoToDao(partnershipDto)).thenReturn(partnershipDaoAfterPatch);
            when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

            // when
            service.updateLimitedPartnership(transaction, SUBMISSION_ID, new PartnershipPatchDto(), REQUEST_ID, USER_ID);

            // then: only one save (the patched DAO) — no rollback save
            verify(repository, times(1)).save(submissionCaptor.capture());
            assertEquals(partnershipDaoAfterPatch, submissionCaptor.getValue());
        }
    }
}
