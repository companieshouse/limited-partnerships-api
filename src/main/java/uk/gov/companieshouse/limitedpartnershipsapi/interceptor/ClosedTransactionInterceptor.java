package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;

import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;

/**
 * ClosedTransactionInterceptor is responsible for handling transaction status checks
 * to determine whether request is allowed based on the transaction's status.
 * It extends AbstractTransactionStatusInterceptor to provide specific behavior
 * for transaction status handling.
 * <p>
 * Allowed scenarios:
 * - The transaction status is 'closed'.
 */
@Component
public class ClosedTransactionInterceptor extends AbstractTransactionStatusInterceptor {

    /**
     * Handles the transaction status and determines whether request is allowed.
     * If the transaction status is CLOSED, request is allowed; otherwise, it is disallowed.
     *
     * @param transaction The transaction object containing the status to be checked.
     * @param reqId The request ID for logging purposes.
     * @param logMap A map containing log context information.
     * @param request The HTTP request object.
     * @param response The HTTP response object.
     * @return true if request is allowed (transaction status is CLOSED), false otherwise.
     */
    @Override
    boolean handleTransactionStatus(Transaction transaction, String reqId, HashMap<String, Object> logMap, HttpServletRequest request, HttpServletResponse response) {
        if (CLOSED.equals(transaction.getStatus())) {
            ApiLogger.infoContext(reqId, "Transaction status is closed - request allowed", logMap);

            return true;
        }

        ApiLogger.errorContext(reqId, "Transaction status is not closed - request disallowed", null, logMap);

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        return false;
    }
}
