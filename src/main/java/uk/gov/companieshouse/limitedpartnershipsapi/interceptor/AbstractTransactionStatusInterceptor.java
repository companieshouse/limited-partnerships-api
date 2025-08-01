package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;
import java.util.Objects;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTION_KEY;


/**
 * An abstract class which can be extended by request interceptors to implement specific behaviour, based on the status
 * of the transaction.
 * <p/>
 * Note that a transaction object instance is expected to already be present in the request attributes. Any concrete
 * implementations must therefore run after the <code>uk.gov.companieshouse.api.interceptor.TransactionInterceptor</code> has completed.
 */
public abstract class AbstractTransactionStatusInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);
        ApiLogger.debugContext(reqId, "Called AbstractTransactionStatusInterceptor.preHandle(...)", null);

        final var transaction = (Transaction) request.getAttribute(TRANSACTION_KEY);

        if (Objects.isNull(transaction)) {
            ApiLogger.errorContext(reqId, "No transaction found in request - action disallowed", null);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return false;
        }

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        return handleTransactionStatus(transaction, reqId, logMap, request, response);
    }

    abstract boolean handleTransactionStatus(Transaction transaction, String reqId, HashMap<String, Object> logMap, HttpServletRequest request, HttpServletResponse response);
}
