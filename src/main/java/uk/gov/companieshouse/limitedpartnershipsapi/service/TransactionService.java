package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.TRANSACTIONS_PRIVATE_API_URI_PREFIX;

@Service
public class TransactionService {

    private final ApiClientService apiClientService;

    @Autowired
    public TransactionService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public void updateTransaction(Transaction transaction, String loggingContext) throws ServiceException {
        try {
            var uri = TRANSACTIONS_PRIVATE_API_URI_PREFIX + transaction.getId();

            // The internal API key client is used here as the transaction service will potentially call back into the
            // LP API (e.g. to get the costs, if a costs end-point has already been set on the transaction) and those
            // calls cannot be made with a user OAuth token
            var response = apiClientService.getInternalApiClient()
                    .privateTransaction().patch(uri, transaction).execute();

            if (response.getStatusCode() != HttpStatus.NO_CONTENT.value()) {
                throw new IOException("Invalid status code received from the Transactions API: " + response.getStatusCode());
            }
        } catch (IOException | URIValidationException e) {
            var message = "Error Updating transaction " + transaction.getId();
            ApiLogger.errorContext(loggingContext, message, e);
            throw new ServiceException(message, e);
        }
    }

    public void updateTransactionWithPartnershipName(Transaction transaction,
                                                     String requestId,
                                                     String partnershipName) throws ServiceException {
        transaction.setCompanyName(partnershipName);
        updateTransaction(transaction, requestId);
    }

    public void deleteTransactionResource(String transactionId, String resourceId, String loggingContext) throws ServiceException {
        try {

            var uri = TRANSACTIONS_PRIVATE_API_URI_PREFIX + transactionId + "/resources";

            var response = apiClientService.getInternalApiClient()
                    .privateTransaction().delete(uri, resourceId).execute();

            if (response.getStatusCode() != HttpStatus.NO_CONTENT.value()) {
                throw new IOException("Invalid status code received from the Transactions API: " + response.getStatusCode());
            }
        } catch (IOException | URIValidationException e) {
            var message = "Error deleting sub-resource " + resourceId + " from transaction " + transactionId;
            ApiLogger.errorContext(loggingContext, message, e);
            throw new ServiceException(message, e);
        }
    }
}
