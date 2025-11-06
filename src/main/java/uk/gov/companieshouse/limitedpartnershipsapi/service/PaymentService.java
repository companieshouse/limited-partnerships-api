package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

@Service
public class PaymentService {

    private final ApiClientService apiClientService;

    @Autowired
    public PaymentService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public PaymentApi getPayment(String paymentReference) throws ServiceException {
        if (!StringUtils.hasText(paymentReference)) {
            throw new ServiceException("Payment Reference cannot be null or empty");
        }

        try {
            return apiClientService
                    .getInternalApiClient()
                    .payment()
                    .get("/payments/" + paymentReference)
                    .execute()
                    .getData();
        } catch (URIValidationException | ApiErrorResponseException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
}
