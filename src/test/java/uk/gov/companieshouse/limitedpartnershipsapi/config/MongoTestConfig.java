package uk.gov.companieshouse.limitedpartnershipsapi.config;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@Profile("test")
public class MongoTestConfig {
    public static final LocalDateTime TEST_DATE_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(TEST_DATE_TIME);
    }
}
