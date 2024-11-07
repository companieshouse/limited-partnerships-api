package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.limitedpartnershipsapi.LimitedPartnershipsApiApplication;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;

import java.util.Collections;

import static uk.gov.companieshouse.api.util.security.Permission.Key.COMPANY_INCORPORATION;


@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;


    public InterceptorConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    /**
     * Set up the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(new CRUDAuthenticationInterceptor(COMPANY_INCORPORATION, Permission.Value.CREATE));
        registry.addInterceptor(new UserAuthenticationInterceptor(Collections.EMPTY_LIST,
                Collections.singletonList("oauth2"),
                new InternalUserInterceptor(LimitedPartnershipsApiApplication.APP_NAMESPACE)));
    }
}
