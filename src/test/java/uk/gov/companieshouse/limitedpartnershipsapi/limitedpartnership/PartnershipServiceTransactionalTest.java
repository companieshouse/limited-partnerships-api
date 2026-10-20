package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.PartnershipBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipRepository;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.PartnershipService;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dao.PartnershipDao;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipDto;
import uk.gov.companieshouse.limitedpartnershipsapi.partnership.dto.PartnershipPatchDto;
import uk.gov.companieshouse.limitedpartnershipsapi.shared.service.TransactionService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PartnershipServiceTransactionalTest {
	Transaction transaction = new TransactionBuilder().build();

	private static final String USER_ID = "xbJf0l";
	private static final String REQUEST_ID = "fd4gld5h3jhh";
	private static final String SUBMISSION_ID = PartnershipBuilder.SUBMISSION_ID;

	@Autowired
	private PartnershipService partnershipService;

	@MockitoBean
	private PartnershipRepository partnershipRepository;

	@MockitoBean
	private TransactionService transactionService;

	@Captor
	private ArgumentCaptor<PartnershipDao> submissionCaptor;

	@Test
	void givenTransactionUpdateFails_whenCreateLP_thenInsertedLPIsDeleted() throws Exception {
		// given
		PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
		PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

		when(partnershipRepository.insert(any(PartnershipDao.class))).thenReturn(partnershipDao);
		doThrow(new ServiceException("Transaction update failed"))
				.when(transactionService).updateTransactionWithLinksAndPartnershipName(
						eq(transaction), any(), any(), any(), any(), any());

		// when + then
		assertThrows(ServiceException.class,
				() -> partnershipService.createLimitedPartnership(transaction, partnershipDto, REQUEST_ID, USER_ID));

		verify(partnershipRepository).deleteById(SUBMISSION_ID);
	}

	@Test
	void givenTransactionUpdateSucceeds_whenCreateLP_thenInsertedLPIsNotDeleted() throws Exception {
		// given
		PartnershipDto partnershipDto = new PartnershipBuilder().buildDto();
		PartnershipDao partnershipDao = new PartnershipBuilder().buildDao();

		when(partnershipRepository.insert(any(PartnershipDao.class))).thenReturn(partnershipDao);

		// when
		partnershipService.createLimitedPartnership(transaction, partnershipDto, REQUEST_ID, USER_ID);

		// then
		verify(partnershipRepository, never()).deleteById(any());
	}

	@Test
	void givenTransactionUpdateFails_whenUpdateLP_thenOriginalLPIsRestored() throws Exception {
		// given
		PartnershipDao partnershipDaoBeforePatch = new PartnershipBuilder().buildDao();
		partnershipDaoBeforePatch.setCreatedBy("original-creator");

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDaoBeforePatch));
		when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);
		doThrow(new ServiceException("Transaction update failed"))
				.when(transactionService).updateTransactionWithPartnershipName(any(), any(), any());

		// when + then
		assertThrows(ServiceException.class,
				() -> partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, new PartnershipPatchDto(), REQUEST_ID, USER_ID));

		// verify: first save = patched DAO, second save = original DAO (rollback)
		verify(partnershipRepository, times(2)).save(submissionCaptor.capture());
		List<PartnershipDao> savedValues = submissionCaptor.getAllValues();
		assertEquals(partnershipDaoBeforePatch, savedValues.get(1));
	}

	@Test
	void givenTransactionUpdateSucceeds_whenUpdateLP_thenOriginalLPIsNotRestored() throws Exception {
		// given
		PartnershipDao partnershipDaoBeforePatch = new PartnershipBuilder().buildDao();

		when(partnershipRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(partnershipDaoBeforePatch));
		when(transactionService.isTransactionLinkedToResource(any(), any(), any())).thenReturn(true);

		// when
		partnershipService.updateLimitedPartnership(transaction, SUBMISSION_ID, new PartnershipPatchDto(), REQUEST_ID, USER_ID);

		// then: only one save (the patched DAO) — no rollback save
		verify(partnershipRepository, times(1)).save(submissionCaptor.capture());

	}
}
