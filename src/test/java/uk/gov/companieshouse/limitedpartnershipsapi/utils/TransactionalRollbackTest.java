package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.TransactionalRollback.Operation.INSERTION;

class TransactionalRollbackTest {

	private static final String REQUEST_ID = "req-123";
	private static final String SUBMISSION_ID = "sub-456";

	@Test
	void givenTransactionUpdateSucceeds_thenRollbackIsNotCalled() throws ServiceException {
		AtomicBoolean rollbackCalled = new AtomicBoolean(false);

		TransactionalRollback.executeWithTransactionalRollback(
				REQUEST_ID, SUBMISSION_ID,
				() -> { /* success — no-op */ },
				INSERTION,
				() -> rollbackCalled.set(true));

		assertFalse(rollbackCalled.get());
	}

	@Test
	void givenTransactionUpdateFails_thenRollbackIsCalledAndExceptionIsRethrown() {
		AtomicBoolean rollbackCalled = new AtomicBoolean(false);

		assertThrows(ServiceException.class, () ->
				TransactionalRollback.executeWithTransactionalRollback(
						REQUEST_ID, SUBMISSION_ID,
						() -> {
							throw new ServiceException("update failed");
						},
						INSERTION,
						() -> rollbackCalled.set(true)));

		assertTrue(rollbackCalled.get());
	}

	@Test
	void givenRollbackAlsoFails_thenOriginalExceptionIsStillRethrown() {
		assertThrows(ServiceException.class, () ->
				TransactionalRollback.executeWithTransactionalRollback(
						REQUEST_ID, SUBMISSION_ID,
						() -> {
							throw new ServiceException("update failed");
						},
						INSERTION,
						() -> {
							throw new RuntimeException("rollback also failed");
						}));
	}
}
