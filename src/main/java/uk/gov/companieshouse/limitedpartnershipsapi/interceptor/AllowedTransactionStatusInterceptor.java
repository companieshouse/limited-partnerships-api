package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;

import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED_PENDING_PAYMENT;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;

/**
 * OpenOrClosedPendingPaymentTransactionInterceptor is responsible for determining whether a request is allowed
 * based on the transaction status and HTTP method.
 * It extends AbstractTransactionStatusInterceptor to provide specific behavior
 * for transaction status handling.
 * <p>
 * Allowed scenarios:
 * - The transaction status is 'open'.
 * - The transaction status is 'closed pending payment' and the request is a GET request.
 * - The transaction status is 'closed' and the request is a GET request.
 */
@Component
public class AllowedTransactionStatusInterceptor extends AbstractTransactionStatusInterceptor {

    /**
     * Handles the transaction status and determines whether request is allowed.
     *
     * @param transaction The transaction object containing the status to be checked.
     * @param reqId The request ID for logging purposes.
     * @param logMap A map containing log context information.
     * @param request The HTTP request object.
     * @param response The HTTP response object.
     * @return true if request is allowed, false otherwise.
     */
    @Override
    boolean handleTransactionStatus(Transaction transaction, String reqId, HashMap<String, Object> logMap, HttpServletRequest request, HttpServletResponse response) {
        var transactionStatus = transaction.getStatus();
        var requestMethod = request.getMethod();

        boolean allowed =
            OPEN.equals(transactionStatus) ||
            (CLOSED_PENDING_PAYMENT.equals(transactionStatus) && requestMethod.equals(HttpMethod.GET.name())) ||
            (CLOSED.equals(transactionStatus) && requestMethod.equals(HttpMethod.GET.name()));

        if (allowed) {
            ApiLogger.debugContext(reqId, "Transaction status is open, closed pending payment (GET), or closed (GET) - request allowed", logMap);
            return true;
        }

        ApiLogger.errorContext(reqId, "Request disallowed due to transaction status not being allowed", null, logMap);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return false;
    }
}
