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

import static uk.gov.companieshouse.limitedpartnershipsapi.LimitedPartnershipsApiApplication.APP_NAMESPACE;

@Configuration
@ComponentScan("uk.gov.companieshouse.api.interceptor")
public class InterceptorConfig implements WebMvcConfigurer {

    private static final String TRANSACTIONS = "/transactions/**";
    private static final String COSTS = TRANSACTIONS + "/costs";
    private static final String FILINGS = "/private/**/filings";

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
                .addPathPatterns(TRANSACTIONS);
        registry.addInterceptor(customUserAuthenticationInterceptor)
                .addPathPatterns(TRANSACTIONS);
        registry.addInterceptor(transactionInterceptor())
                .addPathPatterns(FILINGS, TRANSACTIONS);
        registry.addInterceptor(internalUserInterceptor)
                .addPathPatterns(FILINGS, COSTS);
    }

    @Bean
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor(APP_NAMESPACE);
    }
}
