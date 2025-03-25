package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String TRANSACTION_ID = "12345678";
    private static final String LOGGING_CONTEXT = "fg4536";
    private static final String PRIVATE_TRANSACTIONS_URL = "/private/transactions/";


    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateTransactionResourceHandler privateTransactionResourceHandler;

    @Mock
    private PrivateTransactionPatch privateTransactionPatch;

    @Mock
    private ApiResponse<Void> apiPatchResponse;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    void init() {
        transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkThrowsURIValidationException() throws IOException, URIValidationException {
        when(privateTransactionPatch.execute()).thenThrow(new URIValidationException("ERROR"));

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkThrowsIOException() throws IOException, URIValidationException {
        when(privateTransactionPatch.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkReturnsAnInvalidHttpCode() throws IOException, URIValidationException {
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(400);

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testUpdatingATransactionIsSuccessful() throws IOException, URIValidationException, ServiceException {
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        try {
            transactionService.updateTransaction(transaction, LOGGING_CONTEXT);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void testUpdatingATransactionNameIsSuccessful() throws IOException, URIValidationException {
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        try {
            transactionService.updateTransactionWithPartnershipName(transaction, "New name", LOGGING_CONTEXT);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }
}
