package uk.gov.companieshouse.limitedpartnershipsapi.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

  @Mock private InterceptorRegistry interceptorRegistry;

  @Mock private InterceptorRegistration interceptorRegistration;

  @Mock private LoggingInterceptor loggingInterceptor;

  @InjectMocks private InterceptorConfig interceptorConfig;

  @Test
  void addInterceptorsTest() {
    when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);

    interceptorConfig.addInterceptors(interceptorRegistry);

    InOrder inOrder = inOrder(interceptorRegistry, interceptorRegistration);
    inOrder.verify(interceptorRegistry).addInterceptor(loggingInterceptor);

    verify(interceptorRegistry, times(1)).addInterceptor(any());
  }
}
