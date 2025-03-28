package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.api.util.security.TokenPermissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;

@ExtendWith(MockitoExtension.class)
class CustomUserAuthenticationInterceptorTest {

    private static final String REQ_ID = "43hj5jh345";
    private static final String TOKEN_PERMISSIONS = "token_permissions";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private TokenPermissions mockTokenPermissions;

    @InjectMocks
    private CustomUserAuthenticationInterceptor userAuthenticationInterceptor;

    @Test
    void testInterceptorReturnsTrueWhenRequestHasCorrectTokenPermission() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);

        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_INCORPORATION, Permission.Value.CREATE)).thenReturn(true);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertTrue(result);
    }

    @Test
    void testInterceptorReturnsFalseWhenRequestHasIncorrectTokenPermission() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);

        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_INCORPORATION, Permission.Value.CREATE)).thenReturn(false);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertFalse(result);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    void testTokenPermissionIsSkippedAndInterceptorReturnsTrueWhenAnApiKeyIsUsed() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getHeader(ERIC_REQUEST_ID_KEY)).thenReturn(REQ_ID);

        when(mockHttpServletRequest.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(SecurityConstants.API_KEY_IDENTITY_TYPE);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertTrue(result);
    }
}
