package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.CustomUserAuthenticationInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Mock
    private LoggingInterceptor loggingInterceptor;

    @Mock
    private CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor;

    @Mock
    private InternalUserInterceptor internalUserInterceptor;

    @InjectMocks
    private InterceptorConfig interceptorConfig;

    @Test
    void addInterceptorsTest() {
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);

        interceptorConfig.addInterceptors(interceptorRegistry);

        InOrder inOrder = inOrder(interceptorRegistry, interceptorRegistration);
        inOrder.verify(interceptorRegistry).addInterceptor(loggingInterceptor);
        inOrder.verify(interceptorRegistry).addInterceptor(any(TokenPermissionsInterceptor.class));
        inOrder.verify(interceptorRegistry).addInterceptor(customUserAuthenticationInterceptor);
        inOrder.verify(interceptorRegistry).addInterceptor(any(TransactionInterceptor.class));
        inOrder.verify(interceptorRegistry).addInterceptor(internalUserInterceptor);

        verify(interceptorRegistry, times(5)).addInterceptor(any());
    }
}
