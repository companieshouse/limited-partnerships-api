package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.payment.PaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payment.request.PaymentGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PaymentResourceHandler paymentResourceHandler;
    @Mock
    private PaymentGet paymentGet;
    @Mock
    private ApiResponse<PaymentApi> apiPaymentResponse;
    @Mock
    private PaymentApi paymentApi;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getPayment_returnsPaymentApi_whenReferenceIsValid() throws Exception {
        String paymentReference = "ABC123";
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.payment()).thenReturn(paymentResourceHandler);
        when(paymentResourceHandler.get("/payments/" + paymentReference)).thenReturn(paymentGet);
        when(paymentGet.execute()).thenReturn(apiPaymentResponse);
        when(apiPaymentResponse.getData()).thenReturn(paymentApi);

        PaymentApi result = paymentService.getPayment(paymentReference);

        assertEquals(paymentApi, result);
    }

    @Test
    void getPayment_throwsServiceException_whenReferenceIsNull() {
        ServiceException ex = assertThrows(ServiceException.class, () -> paymentService.getPayment(null));
        assertEquals("Payment Reference cannot be null or empty", ex.getMessage());
    }

    @Test
    void getPayment_throwsServiceException_whenReferenceIsEmpty() {
        ServiceException ex = assertThrows(ServiceException.class, () -> paymentService.getPayment(""));
        assertEquals("Payment Reference cannot be null or empty", ex.getMessage());
    }

    @Test
    void getPayment_throwsServiceException_whenURIValidationExceptionIsThrownInTryBlock() throws Exception {
        String paymentReference = "ABC123";
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.payment()).thenReturn(paymentResourceHandler);
        when(paymentResourceHandler.get("/payments/" + paymentReference)).thenReturn(paymentGet);
        when(paymentGet.execute()).thenThrow(new URIValidationException("Invalid URI"));


        ServiceException ex = assertThrows(ServiceException.class, () -> paymentService.getPayment(paymentReference));
        assertTrue(ex.getMessage().contains("Invalid URI"));
    }
}
