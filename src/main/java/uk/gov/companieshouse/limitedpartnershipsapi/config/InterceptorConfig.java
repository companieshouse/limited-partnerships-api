package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.CustomUserAuthenticationInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    private final CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor;


    public InterceptorConfig(LoggingInterceptor loggingInterceptor, CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.customUserAuthenticationInterceptor = customUserAuthenticationInterceptor;
    }

    /**
     * Set up the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(customUserAuthenticationInterceptor);
    }
}