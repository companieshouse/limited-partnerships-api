package uk.gov.companieshouse.limitedpartnershipsapi.service;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.limitedpartnershipsapi.builder.CompanyBuilder;
import uk.gov.companieshouse.limitedpartnershipsapi.exception.ServiceException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private static final String COMPANY_NUMBER = "LP123456";

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private CompanyResourceHandler companyResourceHandler;

    @Mock
    private CompanyGet companyGet;

    @Mock
    private ApiResponse<CompanyProfileApi> companyGetResponse;

    @InjectMocks
    private CompanyService companyService;

    @BeforeEach
    void init() {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);

    }

    @Test
    void testGetCompanyIsSuccessful() throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(companyGet.execute()).thenReturn(companyGetResponse);
        when(companyGetResponse.getData()).thenReturn(new CompanyBuilder().build());

        CompanyProfileApi response = companyService.getCompanyProfile(COMPANY_NUMBER);

        assertEquals("TEST LP", response.getCompanyName());
    }

    @Test
    void testGetCompany404() throws IOException, URIValidationException {
        when(companyGet.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(new HttpResponseException.Builder(404, "not found", new HttpHeaders()).setMessage("TEST").build()));

        try {
            companyService.getCompanyProfile(COMPANY_NUMBER);
        } catch (ServiceException e) {
            assertEquals("Company not found: " + COMPANY_NUMBER, e.getMessage());
        }
    }

    @Test
    void testGetCompany500() throws ApiErrorResponseException, URIValidationException {
        when(companyGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("TEST")));

        try {
            companyService.getCompanyProfile(COMPANY_NUMBER);
        } catch (ServiceException e) {
            assertEquals("Error getting company profile: " + COMPANY_NUMBER, e.getMessage());
        }
    }
}
