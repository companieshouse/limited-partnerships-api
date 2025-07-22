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
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionDeleteResource;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String TRANSACTION_ID = "12345678";
    private static final String RESOURCE_ID = "resource1234";
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
    private PrivateTransactionDeleteResource privateTransactionDeleteResource;

    @Mock
    private ApiResponse<Void> apiPatchResponse;

    @Mock
    private ApiResponse<Void> apiDeleteResponse;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    void init() {
        transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkThrowsURIValidationException() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenThrow(new URIValidationException("ERROR"));

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkThrowsIOException() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testServiceExceptionThrownWhenApiClientSdkReturnsAnInvalidHttpCode() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(400);

        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, LOGGING_CONTEXT));
    }

    @Test
    void testUpdatingATransactionIsSuccessful() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        try {
            transactionService.updateTransaction(transaction, LOGGING_CONTEXT);
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

    @Test
    void testUpdatingATransactionNameIsSuccessful() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);

        try {
            transactionService.updateTransactionWithPartnershipName(transaction, "New name", LOGGING_CONTEXT);
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }

    @Test
    void testDeleteTransactionResourceIsSuccessful() throws IOException, URIValidationException, ServiceException {
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenReturn(apiDeleteResponse);
        when(apiDeleteResponse.getStatusCode()).thenReturn(204);

        transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT);
    }

    @Test
    void testDeleteTransactionResourceThrowsServiceExceptionOnURIValidationException() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenThrow(new URIValidationException("ERROR"));

        assertThatThrownBy(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error deleting resource resource1234 from transaction 12345678");
    }

    @Test
    void testDeleteTransactionResourceThrowsServiceExceptionOnIOException() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        assertThatThrownBy(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error deleting resource resource1234 from transaction 12345678");
    }

    @Test
    void testDeleteTransactionResourceThrowsServiceExceptionOnInvalidStatusCode() throws IOException, URIValidationException {
        when(privateTransactionResourceHandler.delete(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID + "/resources", RESOURCE_ID))
                .thenReturn(privateTransactionDeleteResource);
        when(privateTransactionDeleteResource.execute()).thenReturn(apiDeleteResponse);
        when(apiDeleteResponse.getStatusCode()).thenReturn(400);

        assertThatThrownBy(() -> transactionService.deleteTransactionResource(TRANSACTION_ID, RESOURCE_ID, LOGGING_CONTEXT))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error deleting resource resource1234 from transaction 12345678");
    }
}
