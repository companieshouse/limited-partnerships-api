package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AbstractTransactionStatusInterceptorTest {

    public static final String TRANSACTION_ATTRIBUTE = "transaction";
    public static final String REQ_ID = "reqId";
    private AbstractTransactionStatusInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private Object handler;

    @BeforeEach
    void setUp() {
        // Create a concrete implementation for testing
        interceptor = new AbstractTransactionStatusInterceptor() {
            @Override
            boolean handleTransactionStatus(Transaction transaction, String reqId, HashMap<String, Object> logMap, HttpServletRequest request, HttpServletResponse response) {
                return true;
            }
        };
        handler = new Object();
    }

    @Test
    void returnsFalseAndSetsErrorStatusWhenTransactionIsMissing() {
        when(request.getAttribute(TRANSACTION_ATTRIBUTE)).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn(REQ_ID);

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void returnsTrueWhenTransactionIsPresent() {
        Transaction transaction = mock(Transaction.class);
        when(request.getAttribute(TRANSACTION_ATTRIBUTE)).thenReturn(transaction);
        when(request.getHeader(anyString())).thenReturn(REQ_ID);
        when(transaction.getId()).thenReturn("txnId");

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }
}