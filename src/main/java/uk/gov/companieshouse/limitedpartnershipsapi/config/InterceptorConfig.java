package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.CustomUserAuthenticationInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;

import java.util.stream.Stream;

import static uk.gov.companieshouse.limitedpartnershipsapi.LimitedPartnershipsApiApplication.APP_NAMESPACE;

@Configuration
@ComponentScan("uk.gov.companieshouse.api.interceptor")
public class InterceptorConfig implements WebMvcConfigurer {

    private static final String[] CRUD_AND_COST_ENDPOINTS = {
            "/transactions/*/incorporation/limited-partnership/**",
            "/transactions/*/limited-partnership/partnership/**",
            "/transactions/*/limited-partnership/general-partner/**",
            "/transactions/*/limited-partnership/general-partners",
            "/transactions/*/limited-partnership/limited-partner/**",
            "/transactions/*/limited-partnership/limited-partners"
    };

    private static final String[] COST_ENDPOINTS = {
            "/transactions/*/incorporation/limited-partnership/*/costs"
    };

    private static final String[] FILINGS_ENDPOINTS = {
            "/private/transactions/*/incorporation/limited-partnership/*/filings"
            // TODO Include other patterns here when post-transition journeys are implemented
    };

    private static final String[] TRANSACTION_ENDPOINTS = Stream.concat(
            Stream.of(CRUD_AND_COST_ENDPOINTS),
            Stream.of(FILINGS_ENDPOINTS)).toArray(String[]::new);

    private static final String[] INTERNAL_ENDPOINTS = Stream.concat(
            Stream.of(FILINGS_ENDPOINTS),
            Stream.of(COST_ENDPOINTS)).toArray(String[]::new);

    private final LoggingInterceptor loggingInterceptor;

    private final CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor;

    private InternalUserInterceptor internalUserInterceptor;


    public InterceptorConfig(LoggingInterceptor loggingInterceptor,
                             CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor,
                             InternalUserInterceptor internalUserInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.customUserAuthenticationInterceptor = customUserAuthenticationInterceptor;
        this.internalUserInterceptor = internalUserInterceptor;
    }

    /**
     * Set up the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     *
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(new TokenPermissionsInterceptor())
                .addPathPatterns(CRUD_AND_COST_ENDPOINTS);
        registry.addInterceptor(customUserAuthenticationInterceptor)
                .addPathPatterns(CRUD_AND_COST_ENDPOINTS);
        registry.addInterceptor(transactionInterceptor())
                .addPathPatterns(TRANSACTION_ENDPOINTS);
        registry.addInterceptor(internalUserInterceptor)
                .addPathPatterns(INTERNAL_ENDPOINTS);
    }

    @Bean
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor(APP_NAMESPACE);
    }
}
