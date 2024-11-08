package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import java.util.HashMap;
import java.util.Optional;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;

public class CustomUserAuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);

        if (skipTokenPermissionChecksWhenApiKeyUsed(reqId, request)) {
            return true;
        }

        // TokenPermissions should have been set up in the request by TokenPermissionsInterceptor
        final var tokenPermissions = getTokenPermissions(request)
                .orElseThrow(() -> new IllegalStateException("UserAuthenticationInterceptor - TokenPermissions object not present in request"));

        boolean hasCompanyIncorporationCreatePermission = tokenPermissions.hasPermission(Permission.Key.COMPANY_INCORPORATION, Permission.Value.CREATE);

        var authInfoMap = new HashMap<String, Object>();
        authInfoMap.put("request_method", request.getMethod());
        authInfoMap.put("has_company_incorporation_create_permission", hasCompanyIncorporationCreatePermission);

        if (hasCompanyIncorporationCreatePermission) {
            ApiLogger.debugContext(reqId, "UserAuthenticationInterceptor authorised with company_incorporation=create permission",
                    authInfoMap);
            return true;
        }
        ApiLogger.infoContext(reqId, "UserAuthenticationInterceptor unauthorised", authInfoMap);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private boolean skipTokenPermissionChecksWhenApiKeyUsed(String reqId, HttpServletRequest request) {
        var logMap = new HashMap<String, Object>();

        if (SecurityConstants.API_KEY_IDENTITY_TYPE.equals(AuthorisationUtil.getAuthorisedIdentityType(request))) {
            ApiLogger.debugContext(reqId, "UserAuthenticationInterceptor skipping token permission checks for api key request", logMap);
            return true;
        }
        return false;
    }

    protected Optional<TokenPermissions> getTokenPermissions(HttpServletRequest request) {
        return AuthorisationUtil.getTokenPermissions(request);
    }
}
