package uk.gov.companieshouse.limitedpartnershipsapi.partnership;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.shared.FilingMode.REGISTRATION;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.FILING_KIND_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.URL_GET_PARTNERSHIP;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceTest {

    private static final String USER_ID = "xbJf0l";
    private static final String REQUEST_ID = "fd4gld5h3jhh";
    private static final String LINK_SELF = "self";
    private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;
    private static final String TRANSACTION_ID = TransactionBuilder.TRANSACTION_ID;

    Transaction transaction = new TransactionBuilder().build();

    @Autowired
    private PartnershipService service;

    @MockitoBean
    private PartnershipRepository repository;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private LimitedPartnershipValidator limitedPartnershipValidator;

    @MockitoBean
    private LimitedPartnershipValidationStatus limitedPartnershipValidationStatus;

    @Captor
    private ArgumentCaptor<PartnershipDao> submissionCaptor;

    @Test
    void givenDto_whenCreateLP_thenLPCreatedWithSubmissionIdAndTransactionUpdated() throws Exception {
        // given
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        when(repository.insert(any(PartnershipDao.class))).thenAnswer(invocation -> {
            PartnershipDao dao = invocation.getArgument(0);
            dao.setId(SUBMISSION_ID);
            return dao;
        });

        String submissionId = service.createLimitedPartnership(transaction, partnershipDto, REQUEST_ID, USER_ID);

        // then
        verify(repository).save(submissionCaptor.capture());
        verify(transactionService).updateTransactionWithLinksAndPartnershipName(eq(transaction), any(), any(), any(), any(), any());
        assertEquals(SUBMISSION_ID, submissionId);

        String submissionUri = String.format(URL_GET_PARTNERSHIP, transaction.getId(), SUBMISSION_ID);
        PartnershipDao sentSubmission = submissionCaptor.getValue();
        String sentSubmissionUri = sentSubmission.getLinks().get(LINK_SELF);
        assertEquals(submissionUri, sentSubmissionUri);
    }

    @Test
    void givenTransactionAlreadyAssociatedWithAnLP_whenCreateLP_thenServiceExceptionThrown() {
        // given
        PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();

        when(transactionService.hasExistingLimitedPartnership(transaction)).thenReturn(true);

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

        var limitedPartnershipPatchDto = new PartnershipPatchDto();

        when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
            partnershipDao));
        when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

        // when
        service.updateLimitedPartnership(transaction, SUBMISSION_ID, limitedPartnershipPatchDto, REQUEST_ID, USER_ID);

        // then
        verify(repository).findById(SUBMISSION_ID);
        verify(repository).save(submissionCaptor.capture());

        PartnershipDao sentSubmission = submissionCaptor.getValue();
        assertEquals("5fd36577288e", sentSubmission.getCreatedBy());
        assertEquals(USER_ID, sentSubmission.getUpdatedBy());

        verify(transactionService).updateTransactionWithPartnershipName(transaction, REQUEST_ID, "Asset Strippers");
    }

    @Test
    void giveSubmissionId_whenGetLp_ThenLPRetrieved() throws ResourceNotFoundException {
        // given
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

        when(repository.findById(partnershipDao.getId())).thenReturn(Optional.of(
            partnershipDao));
        when(transactionService.isTransactionLinkedToResource(eq(transaction), any(String.class), eq(FILING_KIND_LIMITED_PARTNERSHIP))).thenReturn(true);

        // when
        PartnershipDto retrievedDto = service.getLimitedPartnership(transaction, SUBMISSION_ID);

        // then
        Assertions.assertThat(partnershipDao.getData())
            .usingRecursiveComparison()
            .isEqualTo(retrievedDto.getData());
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
        PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

        when(transactionService.doesTransactionHaveALimitedPartnership(transaction, FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(true);
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(
            partnershipDao));

        // when
        PartnershipDto retrievedDto = service.getLimitedPartnership(transaction);

        // then
        Assertions.assertThat(partnershipDao.getData())
            .usingRecursiveComparison()
            .isEqualTo(retrievedDto.getData());
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
    void givenTransactionId_whenClearHasPersonWithSignificantControl_thenFieldSetToNullAndSaved() throws ServiceException {
        // given
        PartnershipDao lpDao = new PartnershipBuilder().buildDao();
        lpDao.getData().setHasPersonWithSignificantControl(true);

        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of(lpDao));

        // when
        service.clearHasPersonWithSignificantControl(transaction, REQUEST_ID);

        // then
        verify(repository).save(submissionCaptor.capture());
        assertNull(submissionCaptor.getValue().getData().getHasPersonWithSignificantControl());
    }

    @Test
    void givenInvalidTransactionId_whenClearHasPersonWithSignificantControl_thenResourceNotFoundExceptionThrown() {
        // given
        when(repository.findByTransactionId(transaction.getId())).thenReturn(List.of());

        // when + then
        assertThrows(ResourceNotFoundException.class, () -> service.clearHasPersonWithSignificantControl(transaction, REQUEST_ID));
        verify(repository, never()).save(any());
    }

    @Test
    void givenNoErrorsWithPartnershipData_whenValidateStatus_thenNoErrorsReturned() throws ServiceException {
        // given
        PartnershipDto limitedPartnershipSubmissionDto = new PartnershipBuilder().buildDto();
        PartnershipDao limitedPartnershipSubmissionDao = new PartnershipBuilder().buildDao();

        when(transactionService.doesTransactionHaveALimitedPartnership(transaction, FILING_KIND_LIMITED_PARTNERSHIP)).thenReturn(true);
        when(repository.findByTransactionId(TRANSACTION_ID)).thenReturn(List.of(limitedPartnershipSubmissionDao));
        when(limitedPartnershipValidationStatus.validate(limitedPartnershipSubmissionDto, REGISTRATION)).thenReturn(new ArrayList<>());

        // when
        List<ValidationStatusError> results = service.validateStatus(transaction);

        // then
        assertEquals(0, results.size());
    }

    @Test
    void giveSubmissionIdAndTransactionIdDoNotMatch_whenValidateStatus_ThenResourceNotFoundExceptionThrown() {
        assertThrows(ResourceNotFoundException.class, () -> service.validateStatus(transaction));
    }

    @Nested
    class Transactional {
        @Test
        void givenTransactionUpdateFails_whenCreateLP_thenInsertedLPIsDeleted() throws Exception {
            // given
            PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
            PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

            when(repository.insert(any(PartnershipDao.class))).thenReturn(partnershipDao);
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

            when(repository.insert(any(PartnershipDao.class))).thenReturn(partnershipDao);

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

            when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDaoBeforePatch));
            when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);
            doThrow(new ServiceException("Transaction update failed"))
                .when(transactionService).updateTransactionWithPartnershipName(any(), any(), any());

            // when + then
            assertThrows(ServiceException.class,
                () -> service.updateLimitedPartnership(transaction, SUBMISSION_ID, new PartnershipPatchDto(), REQUEST_ID, USER_ID));

            // verify: first save = patched DAO, second save = original DAO (rollback)
            verify(repository, times(2)).save(submissionCaptor.capture());
            List<PartnershipDao> savedValues = submissionCaptor.getAllValues();
            assertEquals(partnershipDaoBeforePatch, savedValues.get(1));
        }

        @Test
        void givenTransactionUpdateSucceeds_whenUpdateLP_thenOriginalLPIsNotRestored() throws Exception {
            // given
            PartnershipDao partnershipDaoBeforePatch = new PartnershipBuilder().buildDao();

            when(repository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDaoBeforePatch));
            when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

            // when
            service.updateLimitedPartnership(transaction, SUBMISSION_ID, new PartnershipPatchDto(), REQUEST_ID, USER_ID);

            // then: only one save (the patched DAO) — no rollback save
            verify(repository, times(1)).save(submissionCaptor.capture());

        }
    }
}
