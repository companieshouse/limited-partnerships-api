package uk.gov.companieshouse.limitedpartnershipsapi.client;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.io.IOException;

@Component
public class ApiClientServiceImpl implements ApiClientService {


    @Override
    public ApiClient getApiClient() {
        return ApiSdkManager.getSDK();
    }

    @Override
    public ApiClient getApiClient(String ericPassThroughHeader) throws IOException {
        return ApiSdkManager.getSDK(ericPassThroughHeader);
    }

    @Override
    public InternalApiClient getInternalApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }

    @Override
    public InternalApiClient getInternalApiClient(String passthroughHeader) throws IOException {
        return ApiSdkManager.getPrivateSDK(passthroughHeader);
    }
}
