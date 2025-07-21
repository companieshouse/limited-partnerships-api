package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenOrClosedPendingPaymentTransactionInterceptorTest {

    public static final String REQ_ID = "reqId";
    private OpenOrClosedPendingPaymentTransactionInterceptor interceptor;
    private HashMap<String, Object> logMap;

    @Mock
    private Transaction transaction;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new OpenOrClosedPendingPaymentTransactionInterceptor();
        logMap = new HashMap<>();
    }

    @Test
    void returnsTrueWhenTransactionIsOpen() {
        when(transaction.getStatus()).thenReturn(TransactionStatus.OPEN);

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void returnsTrueWhenTransactionIsClosedPendingPaymentAndGetRequest() {
        when(transaction.getStatus()).thenReturn(TransactionStatus.CLOSED_PENDING_PAYMENT);
        when(request.getMethod()).thenReturn("GET");

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void returnsFalseWhenTransactionIsClosedPendingPaymentAndNotGetRequest() {
        when(transaction.getStatus()).thenReturn(TransactionStatus.CLOSED_PENDING_PAYMENT);
        when(request.getMethod()).thenReturn("POST");

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = { "CLOSED", "DELETED" })
    void returnsFalseWhenTransactionIsClosedOrDeletedStatus(TransactionStatus status) {
        when(transaction.getStatus()).thenReturn(status);

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}