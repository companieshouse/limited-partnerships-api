package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowedTransactionInterceptorTest {

    public static final String REQ_ID = "reqId";
    private AllowedTransactionStatusInterceptor interceptor;
    private HashMap<String, Object> logMap;

    @Mock
    private Transaction transaction;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new AllowedTransactionStatusInterceptor();
        logMap = new HashMap<>();
    }

    @Test
    void returnsTrueWhenTransactionIsOpen() {
        when(transaction.getStatus()).thenReturn(TransactionStatus.OPEN);

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = { "CLOSED_PENDING_PAYMENT", "CLOSED" })
    void returnsTrueWhenTransactionIsClosedOrClosedPendingPaymentAndGetRequest(TransactionStatus status) {
        when(transaction.getStatus()).thenReturn(status);
        when(request.getMethod()).thenReturn("GET");

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @ParameterizedTest
    @MethodSource("disallowedStatusAndMethodProvider")
    void returnsFalseWhenTransactionStatusAndRequestCombinationIsNotAllowed(TransactionStatus status, String method) {
        when(transaction.getStatus()).thenReturn(status);
        when(request.getMethod()).thenReturn(method);

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    static Stream<Arguments> disallowedStatusAndMethodProvider() {
        return Stream.of(
                Arguments.of(TransactionStatus.CLOSED_PENDING_PAYMENT, "POST"),
                Arguments.of(TransactionStatus.CLOSED_PENDING_PAYMENT, "PATCH"),
                Arguments.of(TransactionStatus.CLOSED_PENDING_PAYMENT, "PUT"),
                Arguments.of(TransactionStatus.CLOSED_PENDING_PAYMENT, "DELETE"),
                Arguments.of(TransactionStatus.CLOSED, "POST"),
                Arguments.of(TransactionStatus.CLOSED, "PATCH"),
                Arguments.of(TransactionStatus.CLOSED, "PUT"),
                Arguments.of(TransactionStatus.CLOSED, "DELETE")
        );
    }

    @Test
    void returnsFalseWhenTransactionIsDeletedStatus() {
        when(transaction.getStatus()).thenReturn(TransactionStatus.DELETED);

        boolean result = interceptor.handleTransactionStatus(transaction, REQ_ID, logMap, request, response);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}