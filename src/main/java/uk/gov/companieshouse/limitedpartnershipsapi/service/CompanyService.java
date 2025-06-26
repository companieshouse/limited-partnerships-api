package uk.gov.companieshouse.limitedpartnershipsapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

@Service
public class CompanyService {
    private final ApiClientService apiClientService;

    public CompanyService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public CompanyProfileApi getCompanyProfile(String companyNumber) throws ServiceException {
        try {
            return apiClientService.getInternalApiClient()
                    .company()
                    .get("/company/" + companyNumber)
                    .execute()
                    .getData();

        } catch (ApiErrorResponseException e) {
            if (e.getStatusCode() == 404) {
                var message = "Company not found: " + companyNumber;
                ApiLogger.errorContext(null, message, e);
                throw new ServiceException(message, e);
            }

            var message = "Error getting company profile: " + companyNumber;
            ApiLogger.errorContext(null, message, e);
            throw new ServiceException(message, e);
        } catch (URIValidationException e) {
            var message = "Error getting company profile: " + companyNumber;
            ApiLogger.errorContext(null, message, e);
            throw new ServiceException(message, e);
        }
    }
}
