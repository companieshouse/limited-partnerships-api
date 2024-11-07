package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;
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

    @InjectMocks
    private InterceptorConfig interceptorConfig;

    @Test
    void addInterceptorsTest() {
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);

        interceptorConfig.addInterceptors(interceptorRegistry);

        InOrder inOrder = inOrder(interceptorRegistry, interceptorRegistration);
        inOrder.verify(interceptorRegistry).addInterceptor(loggingInterceptor);

        inOrder.verify(interceptorRegistry).addInterceptor(any(TokenPermissionsInterceptor.class));
        inOrder.verify(interceptorRegistry).addInterceptor(any(UserAuthenticationInterceptor.class));

        verify(interceptorRegistry, times(3)).addInterceptor(any());
    }
}
