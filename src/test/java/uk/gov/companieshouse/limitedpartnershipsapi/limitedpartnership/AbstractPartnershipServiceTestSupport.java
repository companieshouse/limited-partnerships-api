package uk.gov.companieshouse.limitedpartnershipsapi.limitedpartnership;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.CompanyBuilder;

import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder.COMPANY_NUMBER;
import static uk.gov.companieshouse.limitedpartnershipsapi.builder.TransactionBuilder.TRANSACTION_ID;

abstract class AbstractPartnershipServiceTestSupport {

    protected static final String PRIVATE_TRANSACTIONS_URL = "/private/transactions/";

    @MockitoBean
    protected ApiClientService apiClientService;

    @MockitoBean
    protected InternalApiClient internalApiClient;

    @MockitoBean
    protected PrivateTransactionResourceHandler privateTransactionResourceHandler;

    @MockitoBean
    protected PrivateTransactionPatch privateTransactionPatch;

    @MockitoBean
    protected ApiResponse<Void> apiPatchResponse;

    @MockitoBean
    private CompanyResourceHandler companyResourceHandler;

    @MockitoBean
    private CompanyGet companyGet;

    @MockitoBean
    private ApiResponse<CompanyProfileApi> companyGetResponse;

    protected void mockTransactionServicePatchRequest(Transaction transaction)
            throws URIValidationException, ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(PRIVATE_TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(apiPatchResponse);
        when(apiPatchResponse.getStatusCode()).thenReturn(204);
    }

    protected void mockCompanyServiceGetRequest()
        throws URIValidationException, ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);
        when(companyGet.execute()).thenReturn(companyGetResponse);
        when(companyGetResponse.getData()).thenReturn(new CompanyBuilder().build());
    }
}


