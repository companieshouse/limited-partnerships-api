package uk.gov.companieshouse.limitedpartnershipsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.CustomUserAuthenticationInterceptor;
import uk.gov.companieshouse.limitedpartnershipsapi.interceptor.LoggingInterceptor;

import static uk.gov.companieshouse.limitedpartnershipsapi.LimitedPartnershipsApiApplication.APP_NAMESPACE;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private static final String TRANSACTIONS = "/transactions/**";
    private static final String FILINGS = "/private/**/filings";
    private static final String INCORPORATION = TRANSACTIONS + "/incorporation/**";
    private static final String PARTNERSHIP = TRANSACTIONS + "/partnership";

    private final LoggingInterceptor loggingInterceptor;

    private final CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor;

    public InterceptorConfig(LoggingInterceptor loggingInterceptor,
                             CustomUserAuthenticationInterceptor customUserAuthenticationInterceptor) {
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
        registry.addInterceptor(new TokenPermissionsInterceptor())
                .addPathPatterns(INCORPORATION, PARTNERSHIP);
        registry.addInterceptor(customUserAuthenticationInterceptor)
                .addPathPatterns(INCORPORATION, PARTNERSHIP);
        registry.addInterceptor(transactionInterceptor())
                .addPathPatterns(FILINGS, TRANSACTIONS);
    }

    @Bean
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor(APP_NAMESPACE);
    }
}
