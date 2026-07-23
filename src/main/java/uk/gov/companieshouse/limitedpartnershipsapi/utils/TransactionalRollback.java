package uk.gov.companieshouse.limitedpartnershipsapi.utils;

import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

/**
 * Utility class providing a reusable pattern for executing a transaction service update
 * and rolling back any MongoDB writes if the update fails.
 */
public class TransactionalRollback {

    private TransactionalRollback() {
        // Private constructor to prevent instantiation
    }

    /**
     * A functional interface for a runnable that may throw a {@link ServiceException}.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws ServiceException;
    }

    /**
     * Identifies the type of MongoDB operation being rolled back, used in log messages.
     */
    public enum Operation {
        INSERTION("insertion"),
        DELETION("deletion"),
        UPDATE("update");

        private final String description;

        Operation(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Executes a transaction service update and, if it fails with a {@link ServiceException},
     * runs the provided rollback action to revert any MongoDB writes made before the update.
     * The original exception is always re-thrown so the caller is aware of the failure.
     *
     * <p>If the rollback itself fails, that failure is logged independently so the original
     * exception is not masked.</p>
     *
     * @param requestId         the logging context identifier
     * @param submissionId      the MongoDB document ID, used in log messages
     * @param transactionUpdate the transaction service call to execute
     * @param operation         the type of operation being rolled back, used in log messages
     * @param rollback          the action that reverts the MongoDB write(s)
     * @throws ServiceException if the transaction update fails
     */
    public static void executeWithTransactionalRollback(
            String requestId,
            String submissionId,
            ThrowingRunnable transactionUpdate,
            TransactionalRollback.Operation operation,
            Runnable rollback) throws ServiceException {
        try {
            transactionUpdate.run();
        } catch (ServiceException e) {
            ApiLogger.errorContext(requestId, String.format(
                "Failed to update transaction for submission with id: %s. Rolling back %s.", submissionId, operation.getDescription()), e);
            try {
                rollback.run();
            } catch (Exception rollbackException) {
                ApiLogger.errorContext(requestId, String.format(
                    "Failed to rollback %s for submission id: %s", operation.getDescription(), submissionId), rollbackException);
            }
            throw e;
        }
    }
}
